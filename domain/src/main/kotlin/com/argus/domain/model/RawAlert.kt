package com.argus.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Normalized domain representation of an incoming alert trigger from an external source.
 *
 * @property id Unique alert tracking identifier.
 * @property teamId Identifier for the target team profile.
 * @property source External provider or trigger mechanism (e.g. sentry, humio, slack).
 * @property title Concise summary or exception title.
 * @property payload Raw serialized event payload.
 * @property receivedAt Timestamp when the alert was ingested.
 */
@Serializable
public data class RawAlert(
    val id: String = UUID.randomUUID().toString(),
    val teamId: String,
    val source: String = "generic",
    val title: String,
    val payload: String = "",
    val receivedAt: Instant = Clock.System.now(),
)
