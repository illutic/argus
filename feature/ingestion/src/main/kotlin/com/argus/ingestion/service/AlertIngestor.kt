package com.argus.ingestion.service

import com.argus.domain.model.RawAlert
import kotlinx.serialization.Serializable

/**
 * Represents the typed outcome of an inbound alert ingestion request.
 */
@Serializable
public sealed interface IngestResult {

    /**
     * The alert was successfully validated and enqueued for asynchronous triage.
     *
     * @property alertId The unique tracking identifier assigned to this alert.
     */
    @Serializable
    public data class Accepted(
        val alertId: String,
    ) : IngestResult

    /**
     * The alert payload failed validation or was rejected by ingestion rules.
     *
     * @property reason Human-readable explanation of why the payload was rejected.
     */
    @Serializable
    public data class Rejected(
        val reason: String,
    ) : IngestResult
}

/**
 * Boundary contract for receiving, normalizing, and enqueuing alert triggers into the triage pipeline.
 */
public interface AlertIngestor {

    /**
     * Ingests a structured alert payload received via an external webhook.
     */
    public suspend fun ingestWebhook(alert: RawAlert): IngestResult

    /**
     * Ingests an interactive trigger initiated from Slack.
     */
    public suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String?,
    ): IngestResult
}
