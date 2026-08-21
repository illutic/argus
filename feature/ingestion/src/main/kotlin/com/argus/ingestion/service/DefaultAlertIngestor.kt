package com.argus.ingestion.service

import com.argus.domain.model.RawAlert
import com.argus.ingestion.queue.AlertQueue
import org.slf4j.LoggerFactory
import java.util.UUID

internal class DefaultAlertIngestor(
    private val alertQueue: AlertQueue,
) : AlertIngestor {
    private val logger = LoggerFactory.getLogger(DefaultAlertIngestor::class.java)

    override suspend fun ingestWebhook(alert: RawAlert): IngestResult {
        if (alert.teamId.isBlank()) {
            logger.warn("Rejected webhook alert: teamId is blank")
            return IngestResult.Rejected("teamId cannot be blank")
        }
        if (alert.title.isBlank()) {
            logger.warn("Rejected webhook alert for team {}: title is blank", alert.teamId)
            return IngestResult.Rejected("title cannot be blank")
        }

        val alertToQueue = if (alert.id.isBlank()) {
            alert.copy(id = UUID.randomUUID().toString())
        } else {
            alert
        }

        logger.info("Ingesting webhook alert: id={}, teamId={}, source={}", alertToQueue.id, alertToQueue.teamId, alertToQueue.source)
        val enqueued = alertQueue.enqueue(alertToQueue)

        return if (enqueued) {
            IngestResult.Accepted(alertToQueue.id)
        } else {
            logger.error("Failed to enqueue alert id={}: queue full or closed", alertToQueue.id)
            IngestResult.Rejected("Ingestion queue unavailable")
        }
    }

    override suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String?,
    ): IngestResult {
        if (teamId.isBlank()) {
            logger.warn("Rejected Slack trigger: teamId is blank")
            return IngestResult.Rejected("teamId cannot be blank")
        }
        if (command.isBlank()) {
            logger.warn("Rejected Slack trigger for team {}: command is blank", teamId)
            return IngestResult.Rejected("command cannot be blank")
        }

        val alertId = UUID.randomUUID().toString()
        val title = "$command ${text.orEmpty()}".trim()
        val alert = RawAlert(
            id = alertId,
            teamId = teamId,
            source = "slack",
            title = title,
            payload = text.orEmpty(),
        )

        logger.info("Ingesting Slack trigger: id={}, teamId={}, command={}", alertId, teamId, command)
        val enqueued = alertQueue.enqueue(alert)

        return if (enqueued) {
            IngestResult.Accepted(alertId)
        } else {
            logger.error("Failed to enqueue Slack alert id={}", alertId)
            IngestResult.Rejected("Ingestion queue unavailable")
        }
    }
}
