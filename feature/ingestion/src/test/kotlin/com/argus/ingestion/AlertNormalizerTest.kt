package com.argus.ingestion

import com.argus.ingestion.normalizer.DefaultAlertNormalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlertNormalizerTest {

    @Test
    fun `normalizes json payload into structured raw alert`() {
        val normalizer = DefaultAlertNormalizer()
        val jsonPayload = """
            {
                "title": "Database connection pool exhausted",
                "source": "humio",
                "details": "Pool size 50 reached maximum"
            }
        """.trimIndent()

        val alert = normalizer.normalize(
            teamId = "infra-team",
            defaultSource = "humio",
            rawPayload = jsonPayload,
        )

        assertEquals("infra-team", alert.teamId)
        assertEquals("humio", alert.source)
        assertEquals("Database connection pool exhausted", alert.title)
        assertTrue(alert.id.isNotBlank())
    }

    @Test
    fun `normalizes unstructured raw text into title and payload`() {
        val normalizer = DefaultAlertNormalizer()
        val rawText = "Fatal memory error in node-cluster-3"

        val alert = normalizer.normalize(
            teamId = "infra-team",
            defaultSource = "syslog",
            rawPayload = rawText,
        )

        assertEquals("infra-team", alert.teamId)
        assertEquals("syslog", alert.source)
        assertEquals("Fatal memory error in node-cluster-3", alert.title)
        assertEquals(rawText, alert.payload)
    }

    @Test
    fun `handles blank payload gracefully with default title`() {
        val normalizer = DefaultAlertNormalizer()

        val alert = normalizer.normalize(
            teamId = "infra-team",
            defaultSource = "generic",
            rawPayload = "   ",
        )

        assertEquals("infra-team", alert.teamId)
        assertEquals("generic", alert.source)
        assertEquals("Incoming generic alert", alert.title)
    }
}
