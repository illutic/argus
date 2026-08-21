package com.argus.test.fakes

import com.argus.alert.sink.AlertDeliveryResult
import com.argus.alert.sink.AlertSink
import com.argus.domain.model.AlertDecision

class FakeAlertSink : AlertSink {
    val delivered = mutableListOf<AlertDecision>()

    override suspend fun deliver(decision: AlertDecision): AlertDeliveryResult {
        delivered += decision
        return AlertDeliveryResult.Delivered
    }
}
