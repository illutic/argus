package com.argus.config

import com.argus.domain.model.TeamConfig
import com.argus.enrichment.telemetry.TelemetryRegistry

internal sealed interface TeamConfigSyncResult {
    data object Synced : TeamConfigSyncResult

    data class UnregisteredProvider(
        val teamId: String,
        val providerKey: String,
    ) : TeamConfigSyncResult
}

/**
 * Contract: validate every entry in [TeamConfig.telemetry] against
 * [telemetryRegistry], upsert [Teams][com.argus.config.db.Teams] /
 * [TelemetryProviderBinding][com.argus.config.db.TelemetryProviderBinding] rows for a
 * valid config, and crash startup (throw, do not return a result) on the first
 * unregistered provider key — this is the fail-fast contract exercised by
 * `config/teams/_manual-verify-bad-provider.yaml.disabled`.
 * Body withheld: business logic, out of scope for the scaffold pass.
 */
internal class TeamConfigSync(
    private val telemetryRegistry: TelemetryRegistry,
) {
    fun sync(teamConfig: TeamConfig): TeamConfigSyncResult =
        TODO(
            "validate telemetry keys against telemetryRegistry, upsert into SQLite; see class doc for contract",
        )
}
