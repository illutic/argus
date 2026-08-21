package com.argus.enrichment.service

import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.integrations.github.GitHubClient
import com.argus.enrichment.integrations.jira.JiraClient
import com.argus.enrichment.integrations.launchdarkly.LaunchDarklyClient
import com.argus.enrichment.telemetry.TelemetryRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

internal class DefaultAlertEnricher(
    private val telemetryRegistry: TelemetryRegistry,
    private val gitHubClient: GitHubClient,
    private val launchDarklyClient: LaunchDarklyClient,
    private val jiraClient: JiraClient,
) : AlertEnricher {
    override suspend fun enrich(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): EnrichedAlertContext =
        supervisorScope {
            val githubDeferred = async { gitHubClient.query(alert.title) }
            val flagDeferred = async { launchDarklyClient.getFlag(alert.source) }
            val jiraDeferred = async { jiraClient.search("project = ${teamConfig.jiraPrefix}") }

            EnrichedAlertContext(
                alert = alert,
                gitHubSummary = githubDeferred.await().toString(),
                activeFeatureFlags = listOf(flagDeferred.await().toString()),
                relatedJiraTickets = listOf(jiraDeferred.await().toString()),
            )
        }
}
