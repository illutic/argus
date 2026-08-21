package com.argus.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Diagnostic numeric metric sample captured from an external telemetry provider.
 *
 * @property providerKey Source provider key (e.g. humio, sentry, firebase).
 * @property teamId Team identifier.
 * @property name Metric or counter name.
 * @property value Metric value.
 * @property capturedAt Timestamp of the sample capture.
 */
@Serializable
public data class MetricSample(
    val providerKey: String,
    val teamId: String,
    val name: String,
    val value: Double,
    val capturedAt: Instant = Clock.System.now(),
)
