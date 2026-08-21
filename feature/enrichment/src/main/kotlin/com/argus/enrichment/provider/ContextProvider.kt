package com.argus.enrichment.provider

import com.argus.domain.model.AlertContext
import com.argus.domain.model.ProviderKey
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig

/**
 * Pluggable provider contract for retrieving diagnostic context for an alert.
 */
interface ContextProvider {
    val key: ProviderKey
    suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): Result<AlertContext>
}
