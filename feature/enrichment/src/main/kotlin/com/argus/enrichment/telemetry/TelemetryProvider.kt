package com.argus.enrichment.telemetry

import com.argus.domain.model.MetricSample

sealed interface TelemetryFetchResult {
    data class Success(
        val samples: List<MetricSample>,
    ) : TelemetryFetchResult

    data class NotImplemented(
        val providerKey: String,
    ) : TelemetryFetchResult

    data class Failure(
        val providerKey: String,
        val message: String,
    ) : TelemetryFetchResult
}

interface TelemetryProvider {
    val key: String

    suspend fun fetch(teamId: String): TelemetryFetchResult
}

fun interface TelemetryProviderFactory {
    fun create(): TelemetryProvider
}
