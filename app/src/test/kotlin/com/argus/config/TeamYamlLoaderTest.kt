package com.argus.config

import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamYamlLoaderTest {

    @TempDir
    lateinit var tempDir: Path

    private val loader = TeamYamlLoader()

    @Test
    fun `loads and parses valid team YAML file`() {
        val file = tempDir.resolve("payments.yaml").toFile().apply {
            writeText(
                """
                teamId: payments
                jiraPrefix: PAY
                slackChannelId: C12345678
                repoLayers:
                  - services/checkout
                  - services/billing
                telemetry:
                  - sentry
                  - humio
                """.trimIndent(),
            )
        }

        val result = loader.load(file)

        assertTrue(result is TeamYamlLoadResult.Success)
        val config = result.teamConfig
        assertEquals("payments", config.teamId)
        assertEquals("PAY", config.jiraPrefix)
        assertEquals("C12345678", config.slackChannelId)
        assertEquals(listOf("services/checkout", "services/billing"), config.repoLayers)
        assertEquals(listOf("sentry", "humio"), config.telemetry)
    }

    @Test
    fun `loads valid YAML with optional list fields omitted`() {
        val file = tempDir.resolve("minimal.yaml").toFile().apply {
            writeText(
                """
                teamId: core
                jiraPrefix: CORE
                slackChannelId: C88888888
                """.trimIndent(),
            )
        }

        val result = loader.load(file)

        assertTrue(result is TeamYamlLoadResult.Success)
        val config = result.teamConfig
        assertEquals("core", config.teamId)
        assertEquals("CORE", config.jiraPrefix)
        assertEquals("C88888888", config.slackChannelId)
        assertTrue(config.repoLayers.isEmpty())
        assertTrue(config.telemetry.isEmpty())
    }

    @Test
    fun `returns Failure when file does not exist`() {
        val file = File("/path/does/not/exist/team.yaml")

        val result = loader.load(file)

        assertTrue(result is TeamYamlLoadResult.Failure)
        assertEquals(file.path, result.path)
        assertTrue(result.message.contains("File does not exist") || result.message.contains("not found"))
    }

    @Test
    fun `returns Failure when YAML syntax is malformed`() {
        val file = tempDir.resolve("malformed.yaml").toFile().apply {
            writeText(
                """
                teamId: payments
                jiraPrefix: [unclosed list
                """.trimIndent(),
            )
        }

        val result = loader.load(file)

        assertTrue(result is TeamYamlLoadResult.Failure)
        assertEquals(file.path, result.path)
    }

    @Test
    fun `returns Failure when required fields are missing`() {
        val file = tempDir.resolve("missing_fields.yaml").toFile().apply {
            writeText(
                """
                repoLayers:
                  - backend
                """.trimIndent(),
            )
        }

        val result = loader.load(file)

        assertTrue(result is TeamYamlLoadResult.Failure)
    }
}
