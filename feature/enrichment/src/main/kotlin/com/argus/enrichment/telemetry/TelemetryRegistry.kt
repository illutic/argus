package com.argus.enrichment.telemetry

/**
 * Fails fast (per team-config sync contract) when a team YAML references a
 * [key][TelemetryProviderFactory] that was never registered at startup.
 */
class UnregisteredTelemetryProviderException(
    key: String,
) : IllegalStateException("No telemetry provider registered for key '$key'")

interface TelemetryRegistry {
    fun register(
        key: String,
        factory: TelemetryProviderFactory,
    )

    fun resolve(key: String): TelemetryProvider

    fun isRegistered(key: String): Boolean
}
