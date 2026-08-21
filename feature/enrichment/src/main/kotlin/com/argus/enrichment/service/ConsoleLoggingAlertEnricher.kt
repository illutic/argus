package com.argus.enrichment.service

import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.MetricSample
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import org.slf4j.LoggerFactory

internal class ConsoleLoggingAlertEnricher : AlertEnricher {
    private val logger = LoggerFactory.getLogger(ConsoleLoggingAlertEnricher::class.java)

    override suspend fun enrich(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): EnrichedAlertContext {
        logger.info(
            "[Console Enricher] Enriching alert id={}, teamId={}, source={}, title={}",
            alert.id,
            alert.teamId,
            alert.source,
            alert.title,
        )

        return EnrichedAlertContext(
            alert = alert,
            metricSamples = listOf(
                MetricSample(
                    providerKey = alert.source,
                    teamId = alert.teamId,
                    name = "mock_latency_ms",
                    value = 120.0,
                ),
            ),
            recentDeployments = listOf("mock-commit: update dependencies"),
            activeFeatureFlags = listOf("mock-flag: true"),
            relatedJiraTickets = listOf("${teamConfig.jiraPrefix}-101"),
            providerErrors = emptyList(),
        )
    }
}
