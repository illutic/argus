package com.argus.domain.model

import kotlinx.serialization.Serializable

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL,
}

@Serializable
data class AlertDecision(
    val teamId: String,
    val severity: AlertSeverity,
    val summary: String,
    val sourceSampleCount: Int,
)
