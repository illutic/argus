package com.argus.enrichment.provider

import com.argus.domain.model.AlertContext
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig

/**
 * Pluggable provider contract for retrieving diagnostic context for an alert.
 */
public interface ContextProvider {
    public val key: String
    public suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext
}
