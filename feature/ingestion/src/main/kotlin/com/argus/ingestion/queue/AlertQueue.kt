package com.argus.ingestion.queue

import com.argus.domain.model.RawAlert
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Queue boundary allowing asynchronous dispatch of alerts from HTTP intake to background triage workers.
 */
interface AlertQueue {
    suspend fun enqueue(alert: RawAlert): Boolean
    val alerts: ReceiveChannel<RawAlert>
}

/**
 * Thread-safe Kotlin Coroutine Channel implementation of [AlertQueue].
 */
internal class ChannelAlertQueue(
    capacity: Int = 100,
) : AlertQueue {
    private val channel: Channel<RawAlert> = Channel(
        capacity = capacity,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override suspend fun enqueue(alert: RawAlert): Boolean {
        return runCatching {
            channel.send(alert)
            true
        }.getOrDefault(false)
    }

    override val alerts: ReceiveChannel<RawAlert>
        get() = channel
}
