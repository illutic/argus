package com.argus.ingestion.model

import kotlinx.serialization.Serializable

/**
 * Inbound payload representing an alert trigger originating from Slack slash commands or shortcuts.
 */
@Serializable
data class SlackTriggerRequest(
    val teamId: String,
    val command: String,
    val text: String? = null,
)

/**
 * Standard typed JSON response returned by trigger endpoints.
 */
@Serializable
data class IngestionResponse(
    val alertId: String? = null,
    val error: String? = null,
)
