package com.argus.alert

import com.argus.alert.console.ConsoleAlertSink
import com.argus.alert.sink.AlertDeliveryResult
import com.argus.domain.model.AlertDecision
import com.argus.domain.model.AlertSeverity
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleAlertSinkTest {
    @Test
    fun `console alert sink dispatches alert without throwing`() =
        runBlocking {
            val consoleSink = ConsoleAlertSink()
            val decision =
                AlertDecision(
                    teamId = "test-team",
                    severity = AlertSeverity.WARNING,
                    summary = "Test alert summary",
                    sourceSampleCount = 1,
                )
            val result = consoleSink.deliver(decision)
            assertEquals(AlertDeliveryResult.Delivered, result)
        }
}
