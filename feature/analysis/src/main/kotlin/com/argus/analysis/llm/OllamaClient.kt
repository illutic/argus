package com.argus.analysis.llm

import com.argus.domain.model.LlmResult
import io.ktor.client.*

internal class OllamaClient(
    val host: String = "http://localhost:11434",
    val model: String = "gpt-oss:20b",
    private val httpClient: HttpClient? = null,
) : LlmClient {
    override suspend fun complete(prompt: String): LlmResult {
        TODO("POST prompt to $host/api/generate using model $model and parse completion")
    }
}
