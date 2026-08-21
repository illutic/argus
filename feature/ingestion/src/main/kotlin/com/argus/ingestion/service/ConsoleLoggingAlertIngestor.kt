package com.argus.ingestion.service

import org.slf4j.LoggerFactory
import java.util.*

internal class ConsoleLoggingAlertIngestor : AlertIngestor {
    private val logger = LoggerFactory.getLogger(ConsoleLoggingAlertIngestor::class.java)

    override suspend fun ingestWebhook(rawPayload: String): IngestResult {
        val alertId = UUID.randomUUID().toString()
        logger.info("[Ingestion] Webhook received alertId={} payloadLength={}", alertId, rawPayload.length)
        return IngestResult.Accepted(alertId)
    }

    override suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String,
    ): IngestResult {
        val alertId = UUID.randomUUID().toString()
        logger.info(
            "[Ingestion] Slack trigger received alertId={} teamId={} command={} text={}",
            alertId,
            teamId,
            command,
            text,
        )
        return IngestResult.Accepted(alertId)
    }
}
