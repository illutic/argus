package com.argus.test.fakes

import com.argus.analysis.llm.LlmClient
import com.argus.domain.model.LlmResult

class FakeLlmClient(
    var result: LlmResult = LlmResult.Completion("Default mock LLM completion"),
) : LlmClient {
    val recordedPrompts = mutableListOf<String>()

    override suspend fun complete(prompt: String): LlmResult {
        recordedPrompts += prompt
        return result
    }
}
