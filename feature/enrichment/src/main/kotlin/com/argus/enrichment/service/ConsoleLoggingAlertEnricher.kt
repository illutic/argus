package com.argus.enrichment.service

import com.argus.domain.model.AlertContext
import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.ProviderKey
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
            contexts =
                listOf(
                    AlertContext(providerKey = ProviderKey.HUMIO, items = listOf("${alert.source}.latency_ms: 120.0")),
                    AlertContext(providerKey = ProviderKey.GITHUB, items = listOf("mock-commit: update dependencies")),
                    AlertContext(providerKey = ProviderKey.LAUNCH_DARKLY, items = listOf("mock-flag: true")),
                    AlertContext(providerKey = ProviderKey.JIRA, items = listOf("${teamConfig.jiraPrefix}-101")),
                ),
            providerErrors = emptyList(),
        )
    }
}
