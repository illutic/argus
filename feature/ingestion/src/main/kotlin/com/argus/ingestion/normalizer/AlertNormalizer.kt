package com.argus.ingestion.normalizer

import com.argus.domain.model.RawAlert
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/**
 * Strategy interface for parsing incoming third-party payload payloads into canonical [RawAlert] format.
 */
interface AlertNormalizer {
    fun normalize(
        teamId: String,
        defaultSource: String,
        rawPayload: String,
    ): RawAlert
}

internal class DefaultAlertNormalizer(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AlertNormalizer {
    override fun normalize(
        teamId: String,
        defaultSource: String,
        rawPayload: String,
    ): RawAlert {
        if (rawPayload.isBlank()) {
            return RawAlert(
                id = UUID.randomUUID().toString(),
                teamId = teamId,
                source = defaultSource,
                title = "Incoming $defaultSource alert",
                payload = "",
            )
        }

        return runCatching {
            val jsonElement = json.parseToJsonElement(rawPayload).jsonObject
            val title =
                jsonElement["title"]?.jsonPrimitive?.content
                    ?: jsonElement["message"]?.jsonPrimitive?.content
                    ?: jsonElement["summary"]?.jsonPrimitive?.content
                    ?: rawPayload.take(120)

            val source = jsonElement["source"]?.jsonPrimitive?.content ?: defaultSource

            RawAlert(
                id = jsonElement["id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString(),
                teamId = teamId,
                source = source,
                title = title,
                payload = rawPayload,
            )
        }.getOrElse {
            RawAlert(
                id = UUID.randomUUID().toString(),
                teamId = teamId,
                source = defaultSource,
                title = rawPayload.lines().firstOrNull()?.take(120) ?: "Incoming $defaultSource alert",
                payload = rawPayload,
            )
        }
    }
}
