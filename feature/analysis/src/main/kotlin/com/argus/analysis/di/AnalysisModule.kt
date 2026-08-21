package com.argus.analysis.di

import com.argus.analysis.llm.ConsoleEchoLlmClient
import com.argus.analysis.llm.LlmClient
import com.argus.analysis.llm.OllamaClient
import com.argus.analysis.service.LlmTriageEngine
import com.argus.analysis.service.RuleBasedTriageEngine
import com.argus.analysis.service.TriageEngine
import org.koin.core.module.Module
import org.koin.dsl.module

val analysisModule: Module = analysisModule()

fun analysisModule(
    ollamaHost: () -> String = { "http://localhost:11434" },
    ollamaModel: () -> String = { "gpt-oss:20b" },
): Module =
    module {
        single<LlmClient> { OllamaClient(host = ollamaHost(), model = ollamaModel()) }
        single<TriageEngine> { LlmTriageEngine(get()) }
    }

val ruleBasedAnalysisModule: Module =
    module {
        single<TriageEngine> { RuleBasedTriageEngine() }
    }

val consoleAnalysisModule: Module =
    module {
        single<LlmClient> { ConsoleEchoLlmClient() }
        single<TriageEngine> { LlmTriageEngine(get()) }
    }
