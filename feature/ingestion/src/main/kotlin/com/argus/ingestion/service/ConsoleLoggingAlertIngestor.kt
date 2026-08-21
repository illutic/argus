package com.argus.ingestion.service

import com.argus.domain.model.RawAlert
import org.slf4j.LoggerFactory
import java.util.UUID

internal class ConsoleLoggingAlertIngestor : AlertIngestor {
    private val logger = LoggerFactory.getLogger(ConsoleLoggingAlertIngestor::class.java)

    override suspend fun ingestWebhook(alert: RawAlert): IngestResult {
        if (alert.teamId.isBlank()) {
            return IngestResult.Rejected("teamId cannot be blank")
        }
        if (alert.title.isBlank()) {
            return IngestResult.Rejected("title cannot be blank")
        }
        val alertId = if (alert.id.isBlank()) UUID.randomUUID().toString() else alert.id
        logger.info("[Console Ingestor] Webhook alert received: id={}, teamId={}, source={}, title={}", alertId, alert.teamId, alert.source, alert.title)
        return IngestResult.Accepted(alertId)
    }

    override suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String?,
    ): IngestResult {
        if (teamId.isBlank()) {
            return IngestResult.Rejected("teamId cannot be blank")
        }
        if (command.isBlank()) {
            return IngestResult.Rejected("command cannot be blank")
        }
        val alertId = UUID.randomUUID().toString()
        logger.info("[Console Ingestor] Slack trigger received: id={}, teamId={}, command={}, text={}", alertId, teamId, command, text)
        return IngestResult.Accepted(alertId)
    }
}
