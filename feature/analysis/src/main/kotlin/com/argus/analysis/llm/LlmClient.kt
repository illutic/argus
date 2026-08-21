package com.argus.analysis.llm

import com.argus.domain.model.LlmResult

interface LlmClient {
    suspend fun complete(prompt: String): LlmResult
}
