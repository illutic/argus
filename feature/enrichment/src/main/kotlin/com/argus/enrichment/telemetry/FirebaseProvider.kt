package com.argus.enrichment.telemetry

internal class FirebaseProvider : TelemetryProvider {
    override val key: String = KEY

    override suspend fun fetch(teamId: String): TelemetryFetchResult = TelemetryFetchResult.NotImplemented(key)

    companion object {
        const val KEY = "firebase"
    }
}
