package com.argus.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MetricSample(
    val providerKey: String,
    val teamId: String,
    val name: String,
    val value: Double,
    val capturedAt: Instant,
)
