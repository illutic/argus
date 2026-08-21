package com.argus.ingestion.routes

import com.argus.domain.model.RawAlert
import com.argus.ingestion.model.IngestionResponse
import com.argus.ingestion.model.SlackTriggerRequest
import com.argus.ingestion.service.AlertIngestor
import com.argus.ingestion.service.IngestResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

/**
 * Ingestion routing: handles incoming triggers (webhooks, Slack actions).
 */
public fun Route.triggerRoutes() {
    post("/triggers/webhook") {
        val ingestor by call.inject<AlertIngestor>()
        val alert = call.receive<RawAlert>()
        when (val result = ingestor.ingestWebhook(alert)) {
            is IngestResult.Accepted -> call.respond(
                HttpStatusCode.Accepted,
                IngestionResponse(alertId = result.alertId),
            )
            is IngestResult.Rejected -> call.respond(
                HttpStatusCode.BadRequest,
                IngestionResponse(error = result.reason),
            )
        }
    }

    post("/triggers/slack") {
        val ingestor by call.inject<AlertIngestor>()
        val request = call.receive<SlackTriggerRequest>()
        when (val result = ingestor.ingestSlack(request.teamId, request.command, request.text)) {
            is IngestResult.Accepted -> call.respond(
                HttpStatusCode.Accepted,
                IngestionResponse(alertId = result.alertId),
            )
            is IngestResult.Rejected -> call.respond(
                HttpStatusCode.BadRequest,
                IngestionResponse(error = result.reason),
            )
        }
    }
}
