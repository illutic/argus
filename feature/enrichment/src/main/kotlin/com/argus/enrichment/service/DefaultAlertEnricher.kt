package com.argus.enrichment.service

import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.MetricSample
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.integrations.github.GitHubClient
import com.argus.enrichment.integrations.github.GitHubQueryResult
import com.argus.enrichment.integrations.jira.JiraClient
import com.argus.enrichment.integrations.jira.JiraSearchResult
import com.argus.enrichment.integrations.launchdarkly.FlagLookupResult
import com.argus.enrichment.integrations.launchdarkly.LaunchDarklyClient
import com.argus.enrichment.telemetry.TelemetryFetchResult
import com.argus.enrichment.telemetry.TelemetryRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

internal class DefaultAlertEnricher(
    private val telemetryRegistry: TelemetryRegistry,
    private val gitHubClient: GitHubClient,
    private val launchDarklyClient: LaunchDarklyClient,
    private val jiraClient: JiraClient,
) : AlertEnricher {
    private val logger = LoggerFactory.getLogger(DefaultAlertEnricher::class.java)

    override suspend fun enrich(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): EnrichedAlertContext = supervisorScope {
        logger.info("Starting enrichment for alert id={}, teamId={}", alert.id, alert.teamId)

        val metricSamples = CopyOnWriteArrayList<MetricSample>()
        val providerErrors = CopyOnWriteArrayList<String>()
        val recentDeployments = CopyOnWriteArrayList<String>()
        val activeFeatureFlags = CopyOnWriteArrayList<String>()
        val relatedJiraTickets = CopyOnWriteArrayList<String>()

        // 1. Concurrently fetch telemetry from all configured providers for the team
        val telemetryJobs = teamConfig.telemetry.map { providerKey ->
            async {
                runCatching {
                    if (!telemetryRegistry.isRegistered(providerKey)) {
                        val errorMsg = "Telemetry provider '$providerKey' is not registered"
                        logger.warn(errorMsg)
                        providerErrors.add(errorMsg)
                        return@async
                    }

                    val provider = telemetryRegistry.resolve(providerKey)
                    when (val result = provider.fetch(teamConfig.teamId)) {
                        is TelemetryFetchResult.Success -> {
                            metricSamples.addAll(result.samples)
                        }
                        is TelemetryFetchResult.Failure -> {
                            val errorMsg = "Telemetry provider ${result.providerKey}: ${result.message}"
                            logger.warn(errorMsg)
                            providerErrors.add(errorMsg)
                        }
                        is TelemetryFetchResult.NotImplemented -> {
                            logger.debug("Telemetry provider {} returned NotImplemented", result.providerKey)
                        }
                    }
                }.onFailure { ex ->
                    val errorMsg = "Telemetry provider '$providerKey' threw exception: ${ex.message}"
                    logger.error(errorMsg, ex)
                    providerErrors.add(errorMsg)
                }
            }
        }

        // 2. Concurrently query GitHub
        val githubJob = async {
            runCatching {
                when (val result = gitHubClient.query(alert.title)) {
                    is GitHubQueryResult.Success -> {
                        recentDeployments.add(result.payload)
                    }
                    is GitHubQueryResult.Failure -> {
                        val errorMsg = "GitHub query failed for '${result.query}': ${result.message}"
                        logger.warn(errorMsg)
                        providerErrors.add(errorMsg)
                    }
                    is GitHubQueryResult.NotImplemented -> {
                        logger.debug("GitHub client returned NotImplemented")
                    }
                }
            }.onFailure { ex ->
                val errorMsg = "GitHub client exception: ${ex.message}"
                logger.error(errorMsg, ex)
                providerErrors.add(errorMsg)
            }
        }

        // 3. Concurrently query LaunchDarkly
        val ldJob = async {
            runCatching {
                when (val result = launchDarklyClient.getFlag(alert.source)) {
                    is FlagLookupResult.Success -> {
                        activeFeatureFlags.add("${result.flagKey}: ${result.enabled}")
                    }
                    is FlagLookupResult.Failure -> {
                        val errorMsg = "LaunchDarkly lookup failed for '${result.flagKey}': ${result.message}"
                        logger.warn(errorMsg)
                        providerErrors.add(errorMsg)
                    }
                    is FlagLookupResult.NotImplemented -> {
                        logger.debug("LaunchDarkly client returned NotImplemented")
                    }
                }
            }.onFailure { ex ->
                val errorMsg = "LaunchDarkly client exception: ${ex.message}"
                logger.error(errorMsg, ex)
                providerErrors.add(errorMsg)
            }
        }

        // 4. Concurrently query Jira
        val jiraJob = async {
            runCatching {
                val jql = "project = ${teamConfig.jiraPrefix} AND text ~ \"${alert.title}\""
                when (val result = jiraClient.search(jql)) {
                    is JiraSearchResult.Success -> {
                        relatedJiraTickets.addAll(result.issueKeys)
                    }
                    is JiraSearchResult.Failure -> {
                        val errorMsg = "Jira search failed for '${result.jql}': ${result.message}"
                        logger.warn(errorMsg)
                        providerErrors.add(errorMsg)
                    }
                    is JiraSearchResult.NotImplemented -> {
                        logger.debug("Jira client returned NotImplemented")
                    }
                }
            }.onFailure { ex ->
                val errorMsg = "Jira client exception: ${ex.message}"
                logger.error(errorMsg, ex)
                providerErrors.add(errorMsg)
            }
        }

        // Await all concurrent tasks
        telemetryJobs.forEach { it.await() }
        githubJob.await()
        ldJob.await()
        jiraJob.await()

        logger.info(
            "Enrichment completed for alert id={}: metrics={}, deployments={}, flags={}, tickets={}, errors={}",
            alert.id,
            metricSamples.size,
            recentDeployments.size,
            activeFeatureFlags.size,
            relatedJiraTickets.size,
            providerErrors.size,
        )

        EnrichedAlertContext(
            alert = alert,
            metricSamples = metricSamples.toList(),
            recentDeployments = recentDeployments.toList(),
            activeFeatureFlags = activeFeatureFlags.toList(),
            relatedJiraTickets = relatedJiraTickets.toList(),
            providerErrors = providerErrors.toList(),
        )
    }
}
