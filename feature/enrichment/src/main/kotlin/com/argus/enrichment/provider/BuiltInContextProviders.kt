package com.argus.enrichment.provider

import com.argus.domain.model.AlertContext
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

internal class TelemetryContextProvider(
    private val registry: TelemetryRegistry,
) : ContextProvider {
    override val key: String = "telemetry"

    override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext {
        val items = mutableListOf<String>()
        teamConfig.telemetry.forEach { providerKey ->
            if (!registry.isRegistered(providerKey)) {
                error("Telemetry provider '$providerKey' is not registered")
            }
            val provider = registry.resolve(providerKey)
            when (val result = provider.fetch(teamConfig.teamId)) {
                is TelemetryFetchResult.Success -> {
                    result.samples.forEach { sample ->
                        items.add("${sample.providerKey}.${sample.name}: ${sample.value}")
                    }
                }
                is TelemetryFetchResult.Failure -> {
                    error("Telemetry provider '${result.providerKey}': ${result.message}")
                }
                is TelemetryFetchResult.NotImplemented -> Unit
            }
        }
        return AlertContext(providerKey = key, items = items)
    }
}

internal class GitHubContextProvider(
    private val gitHubClient: GitHubClient,
) : ContextProvider {
    override val key: String = "github"

    override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext {
        val items = when (val result = gitHubClient.query(alert.title)) {
            is GitHubQueryResult.Success -> listOf(result.payload)
            is GitHubQueryResult.Failure -> error("GitHub query failed for '${result.query}': ${result.message}")
            is GitHubQueryResult.NotImplemented -> emptyList()
        }
        return AlertContext(providerKey = key, items = items)
    }
}

internal class LaunchDarklyContextProvider(
    private val launchDarklyClient: LaunchDarklyClient,
) : ContextProvider {
    override val key: String = "launchdarkly"

    override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext {
        val items = when (val result = launchDarklyClient.getFlag(alert.source)) {
            is FlagLookupResult.Success -> listOf("${result.flagKey}: ${result.enabled}")
            is FlagLookupResult.Failure -> error("LaunchDarkly lookup failed for '${result.flagKey}': ${result.message}")
            is FlagLookupResult.NotImplemented -> emptyList()
        }
        return AlertContext(providerKey = key, items = items)
    }
}

internal class JiraContextProvider(
    private val jiraClient: JiraClient,
) : ContextProvider {
    override val key: String = "jira"

    override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext {
        val jql = "project = ${teamConfig.jiraPrefix} AND text ~ \"${alert.title}\""
        val items = when (val result = jiraClient.search(jql)) {
            is JiraSearchResult.Success -> result.issueKeys
            is JiraSearchResult.Failure -> error("Jira search failed for '${result.jql}': ${result.message}")
            is JiraSearchResult.NotImplemented -> emptyList()
        }
        return AlertContext(providerKey = key, items = items)
    }
}
