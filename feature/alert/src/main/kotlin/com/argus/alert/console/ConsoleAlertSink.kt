package com.argus.alert.console

import com.argus.alert.sink.AlertDeliveryResult
import com.argus.alert.sink.AlertSink
import com.argus.domain.model.AlertDecision
import org.slf4j.LoggerFactory

internal class ConsoleAlertSink : AlertSink {
    private val logger = LoggerFactory.getLogger(ConsoleAlertSink::class.java)

    override suspend fun deliver(decision: AlertDecision): AlertDeliveryResult {
        val banner =
            buildString {
                appendLine()
                appendLine("========================== [ALERT DISPATCH] ==========================")
                appendLine(" Team ID   : ${decision.teamId}")
                appendLine(" Severity  : ${decision.severity}")
                appendLine(" Samples   : ${decision.sourceSampleCount}")
                appendLine(" Summary   : ${decision.summary}")
                appendLine("======================================================================")
            }
        logger.info(banner)
        return AlertDeliveryResult.Delivered
    }
}
