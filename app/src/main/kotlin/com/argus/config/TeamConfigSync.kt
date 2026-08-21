package com.argus.config

import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider

internal sealed interface TeamConfigSyncResult {
    data object Synced : TeamConfigSyncResult

    data class UnregisteredProvider(
        val teamId: String,
        val providerKey: String,
    ) : TeamConfigSyncResult
}

/**
 * Contract: validate every entry in [TeamConfig.telemetry] against
 * registered [ContextProvider] keys, upsert teams and provider bindings for a
 * valid config, and crash startup (throw, do not return a result) on the first
 * unregistered provider key.
 */
internal class TeamConfigSync(
    private val contextProviders: List<ContextProvider>,
) {
    fun sync(teamConfig: TeamConfig): TeamConfigSyncResult =
        TODO(
            "validate telemetry keys against contextProviders, upsert into SQLite; see class doc for contract",
        )
}
