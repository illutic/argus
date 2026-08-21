package com.argus.ingestion.routes

import com.argus.ingestion.service.AlertIngestor
import com.argus.ingestion.service.IngestResult
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Ingestion routing: handles incoming triggers (webhooks, Slack actions).
 */
fun Route.triggerRoutes() {
    post("/triggers/webhook") {
        val ingestor by call.inject<AlertIngestor>()
        val payload = call.receiveText()
        when (val result = ingestor.ingestWebhook(payload)) {
            is IngestResult.Accepted -> call.respond(HttpStatusCode.Accepted, mapOf("alertId" to result.alertId))
            is IngestResult.Rejected -> call.respond(HttpStatusCode.BadRequest, mapOf("reason" to result.reason))
        }
    }

    post("/triggers/slack") {
        val ingestor by call.inject<AlertIngestor>()
        val payload = call.receiveText()
        when (val result = ingestor.ingestWebhook(payload)) {
            is IngestResult.Accepted -> call.respond(HttpStatusCode.Accepted, mapOf("alertId" to result.alertId))
            is IngestResult.Rejected -> call.respond(HttpStatusCode.BadRequest, mapOf("reason" to result.reason))
        }
    }
}
