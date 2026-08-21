package com.argus.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface LlmResult {
    @Serializable
    data class Completion(
        val text: String,
    ) : LlmResult

    @Serializable
    data class NotImplemented(
        val reason: String,
    ) : LlmResult

    @Serializable
    data class Failure(
        val message: String,
    ) : LlmResult
}
