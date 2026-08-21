package com.argus.ingestion

import com.argus.domain.model.RawAlert
import com.argus.ingestion.queue.ChannelAlertQueue
import com.argus.ingestion.service.DefaultAlertIngestor
import com.argus.ingestion.service.IngestResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultAlertIngestorTest {

    @Test
    fun `nominal webhook alert is accepted and enqueued`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val alert = RawAlert(
            teamId = "payments-team",
            source = "sentry",
            title = "NullPointerException in PaymentProcessor",
            payload = "{\"issueId\": 123}",
        )

        val result = ingestor.ingestWebhook(alert)

        assertIs<IngestResult.Accepted>(result)
        assertTrue(result.alertId.isNotBlank())

        val received = queue.alerts.receive()
        assertEquals("payments-team", received.teamId)
        assertEquals("sentry", received.source)
        assertEquals("NullPointerException in PaymentProcessor", received.title)
        assertEquals(result.alertId, received.id)
    }

    @Test
    fun `webhook alert preserves existing id if provided`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val customId = "custom-alert-999"
        val alert = RawAlert(
            id = customId,
            teamId = "payments-team",
            source = "sentry",
            title = "TimeoutException",
        )

        val result = ingestor.ingestWebhook(alert)

        assertIs<IngestResult.Accepted>(result)
        assertEquals(customId, result.alertId)

        val received = queue.alerts.receive()
        assertEquals(customId, received.id)
    }

    @Test
    fun `nominal slack trigger is accepted and converted to raw alert`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val result = ingestor.ingestSlack(
            teamId = "core-team",
            command = "/triage",
            text = "investigate memory leak in worker node",
        )

        assertIs<IngestResult.Accepted>(result)
        assertTrue(result.alertId.isNotBlank())

        val received = queue.alerts.receive()
        assertEquals("core-team", received.teamId)
        assertEquals("slack", received.source)
        assertEquals("/triage investigate memory leak in worker node", received.title)
        assertEquals("investigate memory leak in worker node", received.payload)
    }

    @Test
    fun `webhook alert with blank teamId is rejected`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val alert = RawAlert(
            teamId = "   ",
            source = "sentry",
            title = "Valid title",
        )

        val result = ingestor.ingestWebhook(alert)

        assertIs<IngestResult.Rejected>(result)
        assertTrue(result.reason.contains("teamId", ignoreCase = true))
    }

    @Test
    fun `webhook alert with blank title is rejected`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val alert = RawAlert(
            teamId = "valid-team",
            source = "sentry",
            title = "",
        )

        val result = ingestor.ingestWebhook(alert)

        assertIs<IngestResult.Rejected>(result)
        assertTrue(result.reason.contains("title", ignoreCase = true))
    }

    @Test
    fun `slack trigger with blank teamId is rejected`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val result = ingestor.ingestSlack(
            teamId = "",
            command = "/triage",
            text = "some text",
        )

        assertIs<IngestResult.Rejected>(result)
        assertTrue(result.reason.contains("teamId", ignoreCase = true))
    }

    @Test
    fun `slack trigger with blank command is rejected`() = runBlocking {
        val queue = ChannelAlertQueue(capacity = 10)
        val ingestor = DefaultAlertIngestor(queue)

        val result = ingestor.ingestSlack(
            teamId = "valid-team",
            command = "  ",
            text = "some text",
        )

        assertIs<IngestResult.Rejected>(result)
        assertTrue(result.reason.contains("command", ignoreCase = true))
    }
}
