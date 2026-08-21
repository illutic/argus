package com.argus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EnrichedAlertContext(
    val alert: RawAlert,
    val telemetrySamples: List<MetricSample> = emptyList(),
    val gitHubSummary: String? = null,
    val activeFeatureFlags: List<String> = emptyList(),
    val relatedJiraTickets: List<String> = emptyList(),
    val providerErrors: List<String> = emptyList(),
)
