package com.argus.domain.model

import kotlinx.serialization.Serializable

/**
 * Aggregated contextual intelligence gathered during the alert enrichment stage.
 *
 * @property alert The original triggering alert.
 * @property metricSamples Numeric metric data points gathered from telemetry providers.
 * @property logs Log lines and trace snippets retrieved from log aggregators (e.g. Humio, Sentry).
 * @property recentDeployments Commits and deployment tags retrieved from GitHub.
 * @property activeFeatureFlags Feature flags and targeting rules retrieved from LaunchDarkly.
 * @property relatedJiraTickets Issue tracking keys and bug summaries retrieved from Jira.
 * @property providerErrors Warning and error messages from any providers that failed or timed out.
 */
@Serializable
public data class EnrichedAlertContext(
    val alert: RawAlert,
    val metricSamples: List<MetricSample> = emptyList(),
    val logs: List<String> = emptyList(),
    val recentDeployments: List<String> = emptyList(),
    val activeFeatureFlags: List<String> = emptyList(),
    val relatedJiraTickets: List<String> = emptyList(),
    val providerErrors: List<String> = emptyList(),
)
