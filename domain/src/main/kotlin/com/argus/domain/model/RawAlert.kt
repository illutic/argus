package com.argus.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RawAlert(
    val id: String,
    val teamId: String,
    val source: String,
    val title: String,
    val payload: String,
    val receivedAt: Instant,
)
