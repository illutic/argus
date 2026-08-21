package com.argus.enrichment

import com.argus.domain.model.AlertContext
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider
import com.argus.enrichment.provider.GitHubContextProvider
import com.argus.enrichment.provider.JiraContextProvider
import com.argus.enrichment.provider.LaunchDarklyContextProvider
import com.argus.enrichment.provider.TelemetryContextProvider
import com.argus.enrichment.service.DefaultAlertEnricher
import com.argus.enrichment.telemetry.TelemetryFetchResult
import com.argus.enrichment.telemetry.TelemetryProvider
import com.argus.domain.model.MetricSample
import com.argus.enrichment.integrations.github.GitHubQueryResult
import com.argus.enrichment.integrations.jira.JiraSearchResult
import com.argus.enrichment.integrations.launchdarkly.FlagLookupResult
import com.argus.test.fakes.FakeGitHubClient
import com.argus.test.fakes.FakeJiraClient
import com.argus.test.fakes.FakeLaunchDarklyClient
import com.argus.test.fakes.FakeTelemetryRegistry
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlertEnricherTest {

    private class StubTelemetryProvider(
        override val key: String,
        private val result: TelemetryFetchResult,
    ) : TelemetryProvider {
        override suspend fun fetch(teamId: String): TelemetryFetchResult = result
    }

    private val alert = RawAlert(
        teamId = "payments",
        source = "sentry",
        title = "NullPointerException in StripeCheckout",
    )

    private val teamConfig = TeamConfig(
        teamId = "payments",
        jiraPrefix = "PAY",
        slackChannelId = "C123",
        telemetry = listOf("sentry", "humio"),
    )

    @Test
    fun `enrich aggregates provider-agnostic contexts across all registered providers`() = runBlocking {
        val registry = FakeTelemetryRegistry().apply {
            register("sentry") {
                StubTelemetryProvider(
                    "sentry",
                    TelemetryFetchResult.Success(
                        listOf(MetricSample(providerKey = "sentry", teamId = "payments", name = "error_rate", value = 12.5)),
                    ),
                )
            }
            register("humio") {
                StubTelemetryProvider(
                    "humio",
                    TelemetryFetchResult.Success(
                        listOf(MetricSample(providerKey = "humio", teamId = "payments", name = "p99_latency", value = 850.0)),
                    ),
                )
            }
        }

        val gitHubClient = FakeGitHubClient(
            result = GitHubQueryResult.Success("commit abc1234: update checkout flow"),
        )
        val ldClient = FakeLaunchDarklyClient(
            result = FlagLookupResult.Success(flagKey = "new-checkout-v2", enabled = true),
        )
        val jiraClient = FakeJiraClient(
            result = JiraSearchResult.Success(listOf("PAY-101: Fix payment timeout")),
        )

        val providers: List<ContextProvider> = listOf(
            TelemetryContextProvider(registry),
            GitHubContextProvider(gitHubClient),
            LaunchDarklyContextProvider(ldClient),
            JiraContextProvider(jiraClient),
        )

        val enricher = DefaultAlertEnricher(providers)
        val context = enricher.enrich(alert, teamConfig)

        assertEquals("payments", context.alert.teamId)
        assertEquals(4, context.contexts.size)
        assertTrue(context.providerErrors.isEmpty())

        val telemetryContext = context.contexts.first { it.providerKey == "telemetry" }
        assertEquals(2, telemetryContext.items.size)
        assertTrue(telemetryContext.items.any { it.contains("sentry.error_rate: 12.5") })

        val githubContext = context.contexts.first { it.providerKey == "github" }
        assertTrue(githubContext.items.first().contains("abc1234"))

        val ldContext = context.contexts.first { it.providerKey == "launchdarkly" }
        assertTrue(ldContext.items.first().contains("new-checkout-v2: true"))

        val jiraContext = context.contexts.first { it.providerKey == "jira" }
        assertTrue(jiraContext.items.first().contains("PAY-101"))
    }

    @Test
    fun `enrich handles individual provider failure gracefully without failing entire pipeline`() = runBlocking {
        val failingProvider = object : ContextProvider {
            override val key: String = "flakey-provider"
            override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext {
                error("Network timeout contacting upstream service")
            }
        }

        val successfulProvider = object : ContextProvider {
            override val key: String = "reliable-provider"
            override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext {
                return AlertContext(providerKey = key, items = listOf("healthy diagnostic data"))
            }
        }

        val enricher = DefaultAlertEnricher(listOf(failingProvider, successfulProvider))
        val context = enricher.enrich(alert, teamConfig)

        assertEquals(1, context.contexts.size)
        assertEquals("reliable-provider", context.contexts.first().providerKey)
        assertEquals(1, context.providerErrors.size)
        assertTrue(context.providerErrors.first().contains("flakey-provider"))
        assertTrue(context.providerErrors.first().contains("Network timeout"))
    }
}
