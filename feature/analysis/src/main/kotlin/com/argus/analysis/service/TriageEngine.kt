package com.argus.analysis.service

import com.argus.domain.model.AlertDecision
import com.argus.domain.model.EnrichedAlertContext

interface TriageEngine {
    suspend fun analyze(context: EnrichedAlertContext): AlertDecision
}
