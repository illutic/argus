package com.argus.enrichment.service

import com.argus.domain.model.AlertContext
import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

internal class DefaultAlertEnricher(
    private val providers: List<ContextProvider>,
) : AlertEnricher {
    private val logger = LoggerFactory.getLogger(DefaultAlertEnricher::class.java)

    override suspend fun enrich(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): EnrichedAlertContext = supervisorScope {
        logger.info("Enriching alert id={}, teamId={} across {} providers", alert.id, alert.teamId, providers.size)

        val contexts = CopyOnWriteArrayList<AlertContext>()
        val providerErrors = CopyOnWriteArrayList<String>()

        providers.map { provider ->
            async {
                provider.fetchContext(alert, teamConfig)
                    .onSuccess { context ->
                        if (context.items.isNotEmpty()) {
                            contexts.add(context)
                        }
                    }
                    .onFailure { ex ->
                        val errorMsg = "Provider '${provider.key}' failed: ${ex.message}"
                        logger.warn(errorMsg, ex)
                        providerErrors.add(errorMsg)
                    }
            }
        }.awaitAll()

        EnrichedAlertContext(
            alert = alert,
            contexts = contexts.toList(),
            providerErrors = providerErrors.toList(),
        )
    }
}
