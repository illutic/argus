package com.argus.alert.slack

import com.argus.alert.sink.AlertDeliveryResult
import com.argus.alert.sink.AlertSink
import com.argus.domain.model.AlertDecision
import org.slf4j.LoggerFactory

internal class SlackAlertSink(
    private val botToken: String = "",
) : AlertSink {
    private val logger = LoggerFactory.getLogger(SlackAlertSink::class.java)

    override suspend fun deliver(decision: AlertDecision): AlertDeliveryResult {
        logger.info("stub Slack delivery for team={} severity={}", decision.teamId, decision.severity)
        return AlertDeliveryResult.NotImplemented(teamId = decision.teamId)
    }
}
