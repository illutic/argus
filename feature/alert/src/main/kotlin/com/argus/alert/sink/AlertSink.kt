package com.argus.alert.sink

import com.argus.domain.model.AlertDecision

sealed interface AlertDeliveryResult {
    data object Delivered : AlertDeliveryResult

    data class NotImplemented(
        val teamId: String,
    ) : AlertDeliveryResult

    data class Failure(
        val teamId: String,
        val message: String,
    ) : AlertDeliveryResult
}

interface AlertSink {
    suspend fun deliver(decision: AlertDecision): AlertDeliveryResult
}
