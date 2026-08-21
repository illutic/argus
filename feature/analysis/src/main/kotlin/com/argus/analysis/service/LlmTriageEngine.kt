package com.argus.analysis.service

import com.argus.analysis.llm.LlmClient
import com.argus.domain.model.AlertDecision
import com.argus.domain.model.AlertSeverity
import com.argus.domain.model.EnrichedAlertContext
import com.argus.domain.model.LlmResult

internal class LlmTriageEngine(
    private val llmClient: LlmClient,
) : TriageEngine {
    override suspend fun analyze(context: EnrichedAlertContext): AlertDecision {
        val prompt = "Triage alert: ${context.alert.title} from ${context.alert.source}"
        val result = llmClient.complete(prompt)
        val summary =
            when (result) {
                is LlmResult.Completion -> result.text
                is LlmResult.NotImplemented -> "Heuristic fallback: ${context.alert.title}"
                is LlmResult.Failure -> "Analysis failure fallback: ${result.message}"
            }
        return AlertDecision(
            teamId = context.alert.teamId,
            severity = AlertSeverity.WARNING,
            summary = summary,
            sourceSampleCount = context.metricSamples.size,
        )
    }
}
