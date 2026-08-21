package com.argus.config

import com.argus.domain.model.ProviderKey
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider
import com.argus.test.fakes.FakeContextProvider
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TeamConfigSyncTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var teamRepository: TeamRepository
    private val providers: List<ContextProvider> = listOf(
        FakeContextProvider(ProviderKey.HUMIO),
        FakeContextProvider(ProviderKey.SENTRY),
        FakeContextProvider(ProviderKey.FIREBASE),
        FakeContextProvider(ProviderKey.GITHUB),
        FakeContextProvider(ProviderKey.LAUNCH_DARKLY),
        FakeContextProvider(ProviderKey.JIRA),
    )

    @BeforeTest
    fun setup() {
        teamRepository = InMemoryTeamRepository()
    }

    @Test
    fun `syncs valid team config and stores in memory repository`() {
        val sync = TeamConfigSync(providers, teamRepository)
        val config = TeamConfig(
            teamId = "payments",
            jiraPrefix = "PAY",
            slackChannelId = "C12345",
            telemetry = listOf("sentry", "humio"),
        )

        val result = sync.sync(config)

        assertEquals(TeamConfigSyncResult.Synced("payments"), result)

        val stored = teamRepository.get("payments")
        assertNotNull(stored)
        assertEquals("payments", stored.teamId)
        assertEquals("PAY", stored.jiraPrefix)
        assertEquals("C12345", stored.slackChannelId)
        assertEquals(listOf("sentry", "humio"), stored.telemetry)
    }

    @Test
    fun `sync does not throw on unregistered telemetry provider key and returns UnregisteredProvider result`() {
        val sync = TeamConfigSync(providers, teamRepository)
        val config = TeamConfig(
            teamId = "bad-team",
            jiraPrefix = "BAD",
            slackChannelId = "C99999",
            telemetry = listOf("unregistered-telemetry-key"),
        )

        val result = sync.sync(config)

        assertTrue(result is TeamConfigSyncResult.UnregisteredProvider)
        assertEquals("bad-team", result.teamId)
        assertEquals("unregistered-telemetry-key", result.providerKey)
        assertNull(teamRepository.get("bad-team"))
    }

    @Test
    fun `syncDirectory syncs valid yaml files and records failures without throwing`() {
        val sync = TeamConfigSync(providers, teamRepository)
        val loader = TeamYamlLoader()

        tempDir.resolve("team1.yaml").toFile().apply {
            writeText(
                """
                teamId: core
                jiraPrefix: CORE
                slackChannelId: C11111
                telemetry:
                  - sentry
                """.trimIndent(),
            )
        }

        tempDir.resolve("invalid-team.yaml").toFile().apply {
            writeText(
                """
                teamId: invalid-team
                jiraPrefix: [malformed
                """.trimIndent(),
            )
        }

        tempDir.resolve("team3.yaml.disabled").toFile().apply {
            writeText(
                """
                teamId: disabled-team
                jiraPrefix: DIS
                slackChannelId: C22222
                """.trimIndent(),
            )
        }

        val results = sync.syncDirectory(tempDir.toFile(), loader)

        assertEquals(2, results.size)
        assertTrue(results.any { it is TeamConfigSyncResult.Synced && it.teamId == "core" })
        assertTrue(results.any { it is TeamConfigSyncResult.LoadFailure })

        val validStored = teamRepository.get("core")
        assertNotNull(validStored)
        assertEquals("core", validStored.teamId)
        assertNull(teamRepository.get("invalid-team"))
    }
}
