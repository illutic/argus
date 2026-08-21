package com.argus.ingestion.service

sealed interface IngestResult {
    data class Accepted(
        val alertId: String,
    ) : IngestResult

    data class Rejected(
        val reason: String,
    ) : IngestResult
}

interface AlertIngestor {
    suspend fun ingestWebhook(rawPayload: String): IngestResult

    suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String,
    ): IngestResult
}
