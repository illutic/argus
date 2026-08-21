package com.argus.domain.model

import kotlinx.serialization.Serializable

/**
 * Diagnostic context provided by an integration or telemetry source.
 *
 * @property providerKey Identifier of the data source (e.g. github, launchdarkly, jira, sentry, humio).
 * @property items Contextual data points, logs, deployment summaries, or issue keys.
 */
@Serializable
public data class AlertContext(
    val providerKey: String,
    val items: List<String> = emptyList(),
)

/**
 * Provider-agnostic aggregated contextual intelligence gathered during alert enrichment.
 *
 * @property alert The original triggering alert.
 * @property contexts List of provider-agnostic context items.
 * @property providerErrors Warning and error messages from any providers that failed or timed out.
 */
@Serializable
public data class EnrichedAlertContext(
    val alert: RawAlert,
    val contexts: List<AlertContext> = emptyList(),
    val providerErrors: List<String> = emptyList(),
)
