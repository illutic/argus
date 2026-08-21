package com.argus.analysis.service

import com.argus.domain.model.AlertDecision
import com.argus.domain.model.AlertSeverity
import com.argus.domain.model.EnrichedAlertContext

internal class RuleBasedTriageEngine : TriageEngine {
    override suspend fun analyze(context: EnrichedAlertContext): AlertDecision {
        val severity =
            if (context.alert.title.contains("CRITICAL", ignoreCase = true)) {
                AlertSeverity.CRITICAL
            } else {
                AlertSeverity.WARNING
            }
        return AlertDecision(
            teamId = context.alert.teamId,
            severity = severity,
            summary = "Rule-based triage: ${context.alert.title} (Source: ${context.alert.source})",
            sourceSampleCount = context.metricSamples.size,
        )
    }
}
