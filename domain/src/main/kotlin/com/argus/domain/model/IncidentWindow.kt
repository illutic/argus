package com.argus.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class IncidentWindow(
    val teamId: String,
    val start: Instant,
    val end: Instant,
    val samples: List<MetricSample>,
)
