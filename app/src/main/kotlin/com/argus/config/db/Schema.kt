package com.argus.config.db

import org.jetbrains.exposed.sql.Table

internal object Teams : Table("teams") {
    val teamId = varchar("team_id", 128)
    val jiraPrefix = varchar("jira_prefix", 32)
    val slackChannelId = varchar("slack_channel_id", 64)

    override val primaryKey = PrimaryKey(teamId)
}

internal object TelemetryProviderBinding : Table("telemetry_provider_binding") {
    val teamId = varchar("team_id", 128).references(Teams.teamId)
    val providerKey = varchar("provider_key", 64)

    override val primaryKey = PrimaryKey(teamId, providerKey)
}
