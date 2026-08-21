package com.argus.enrichment.service

import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig

interface AlertEnricher {
    suspend fun enrich(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): EnrichedAlertContext
}
