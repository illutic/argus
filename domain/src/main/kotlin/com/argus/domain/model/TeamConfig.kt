package com.argus.domain.model

import kotlinx.serialization.Serializable

/**
 * Team routing and telemetry configuration profile.
 *
 * @property teamId Unique team identifier.
 * @property jiraPrefix Issue tracker project key prefix (e.g. CORE, PAY).
 * @property slackChannelId Slack channel destination for triage briefs.
 * @property repoLayers Monitored repository directories/layers.
 * @property telemetry Registered telemetry provider keys active for this team (e.g. humio, sentry, firebase).
 */
@Serializable
data class TeamConfig(
    val teamId: String,
    val jiraPrefix: String,
    val slackChannelId: String,
    val repoLayers: List<String> = emptyList(),
    val telemetry: List<String> = emptyList(),
)
