package com.argus.enrichment.service

import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import org.slf4j.LoggerFactory

internal class ConsoleLoggingAlertEnricher : AlertEnricher {
    private val logger = LoggerFactory.getLogger(ConsoleLoggingAlertEnricher::class.java)

    override suspend fun enrich(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): EnrichedAlertContext {
        logger.info("[Enrichment] Locally enriching alertId={} for team={}", alert.id, teamConfig.teamId)
        return EnrichedAlertContext(
            alert = alert,
            gitHubSummary = "Local mock git deployment",
            activeFeatureFlags = listOf("flag-feature-x: enabled"),
            relatedJiraTickets = listOf("${teamConfig.jiraPrefix}-101"),
        )
    }
}
