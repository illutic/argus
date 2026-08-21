package com.argus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamConfig(
    val teamId: String,
    val jiraPrefix: String,
    val slackChannelId: String,
    val repoLayers: List<String>,
    val telemetry: List<String>,
)
