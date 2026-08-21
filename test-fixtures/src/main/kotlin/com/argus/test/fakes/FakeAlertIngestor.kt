package com.argus.test.fakes

import com.argus.domain.model.RawAlert
import com.argus.ingestion.service.AlertIngestor
import com.argus.ingestion.service.IngestResult
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

public class FakeAlertIngestor(
    private val shouldAccept: Boolean = true,
    private val rejectReason: String = "Test rejection",
) : AlertIngestor {

    public val ingestedAlerts: MutableList<RawAlert> = CopyOnWriteArrayList()

    override suspend fun ingestWebhook(alert: RawAlert): IngestResult {
        if (!shouldAccept) {
            return IngestResult.Rejected(rejectReason)
        }
        if (alert.teamId.isBlank()) {
            return IngestResult.Rejected("teamId cannot be blank")
        }
        if (alert.title.isBlank()) {
            return IngestResult.Rejected("title cannot be blank")
        }
        val alertToSave = if (alert.id.isBlank()) alert.copy(id = UUID.randomUUID().toString()) else alert
        ingestedAlerts.add(alertToSave)
        return IngestResult.Accepted(alertToSave.id)
    }

    override suspend fun ingestSlack(
        teamId: String,
        command: String,
        text: String?,
    ): IngestResult {
        if (!shouldAccept) {
            return IngestResult.Rejected(rejectReason)
        }
        if (teamId.isBlank()) {
            return IngestResult.Rejected("teamId cannot be blank")
        }
        if (command.isBlank()) {
            return IngestResult.Rejected("command cannot be blank")
        }
        val alert = RawAlert(
            id = UUID.randomUUID().toString(),
            teamId = teamId,
            source = "slack",
            title = "$command ${text.orEmpty()}".trim(),
            payload = text.orEmpty(),
        )
        ingestedAlerts.add(alert)
        return IngestResult.Accepted(alert.id)
    }
}
