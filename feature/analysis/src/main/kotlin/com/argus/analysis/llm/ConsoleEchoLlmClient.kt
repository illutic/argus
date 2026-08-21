package com.argus.analysis.llm

import com.argus.domain.model.LlmResult
import org.slf4j.LoggerFactory

internal class ConsoleEchoLlmClient : LlmClient {
    private val logger = LoggerFactory.getLogger(ConsoleEchoLlmClient::class.java)

    override suspend fun complete(prompt: String): LlmResult {
        logger.info("[Local LLM Prompt] {}", prompt)
        val mockCompletion = "[Simulated AI Analysis] Identified potential timeout issue in upstream dependency."
        logger.info("[Local LLM Response] {}", mockCompletion)
        return LlmResult.Completion(mockCompletion)
    }
}
