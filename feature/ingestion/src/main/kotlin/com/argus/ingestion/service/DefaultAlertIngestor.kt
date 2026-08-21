package com.argus.ingestion.service

import java.util.*

internal class DefaultAlertIngestor : AlertIngestor {
    override suspend fun ingestWebhook(rawPayload: String): IngestResult {
        val alertId = UUID.randomUUID().toString()
        return IngestResult.Accepted(alertId)
    }

    override suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String,
    ): IngestResult {
        val alertId = UUID.randomUUID().toString()
        return IngestResult.Accepted(alertId)
    }
}
