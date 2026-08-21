package com.argus.enrichment

import com.argus.domain.model.MetricSample
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.integrations.github.GitHubQueryResult
import com.argus.enrichment.integrations.jira.JiraSearchResult
import com.argus.enrichment.integrations.launchdarkly.FlagLookupResult
import com.argus.enrichment.service.DefaultAlertEnricher
import com.argus.enrichment.telemetry.TelemetryFetchResult
import com.argus.enrichment.telemetry.TelemetryProvider
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
    fun `enrich aggregates complete context across telemetry, github, launchdarkly, and jira`() = runBlocking {
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
            result = GitHubQueryResult.Success("commit abc1234 by dev@company.com: update checkout flow"),
        )
        val ldClient = FakeLaunchDarklyClient(
            result = FlagLookupResult.Success(flagKey = "new-checkout-v2", enabled = true),
        )
        val jiraClient = FakeJiraClient(
            result = JiraSearchResult.Success(listOf("PAY-101: Fix payment timeout", "PAY-102: Upgrade Stripe SDK")),
        )

        val enricher = DefaultAlertEnricher(registry, gitHubClient, ldClient, jiraClient)

        val context = enricher.enrich(alert, teamConfig)

        assertEquals("payments", context.alert.teamId)
        assertEquals(2, context.metricSamples.size)
        assertEquals(1, context.recentDeployments.size)
        assertTrue(context.recentDeployments.first().contains("abc1234"))
        assertEquals(1, context.activeFeatureFlags.size)
        assertTrue(context.activeFeatureFlags.first().contains("new-checkout-v2: true"))
        assertEquals(2, context.relatedJiraTickets.size)
        assertTrue(context.providerErrors.isEmpty())
    }

    @Test
    fun `enrich handles telemetry provider failure gracefully without failing entire pipeline`() = runBlocking {
        val registry = FakeTelemetryRegistry().apply {
            register("sentry") {
                StubTelemetryProvider(
                    "sentry",
                    TelemetryFetchResult.Failure("sentry", "HTTP 503 Service Unavailable"),
                )
            }
            register("humio") {
                StubTelemetryProvider(
                    "humio",
                    TelemetryFetchResult.Success(
                        listOf(MetricSample(providerKey = "humio", teamId = "payments", name = "cpu_usage", value = 92.0)),
                    ),
                )
            }
        }

        val gitHubClient = FakeGitHubClient(GitHubQueryResult.Success("deployment v1.2.3"))
        val ldClient = FakeLaunchDarklyClient(FlagLookupResult.Success("flag-1", false))
        val jiraClient = FakeJiraClient(JiraSearchResult.Success(listOf("PAY-55")))

        val enricher = DefaultAlertEnricher(registry, gitHubClient, ldClient, jiraClient)

        val context = enricher.enrich(alert, teamConfig)

        assertEquals(1, context.metricSamples.size)
        assertEquals("humio", context.metricSamples.first().providerKey)
        assertEquals(1, context.providerErrors.size)
        assertTrue(context.providerErrors.first().contains("sentry: HTTP 503"))
        assertEquals(1, context.recentDeployments.size)
        assertEquals(1, context.relatedJiraTickets.size)
    }

    @Test
    fun `enrich handles unregistered telemetry provider without crashing`() = runBlocking {
        val registry = FakeTelemetryRegistry() // neither sentry nor humio registered
        val gitHubClient = FakeGitHubClient()
        val ldClient = FakeLaunchDarklyClient()
        val jiraClient = FakeJiraClient()

        val enricher = DefaultAlertEnricher(registry, gitHubClient, ldClient, jiraClient)

        val context = enricher.enrich(alert, teamConfig)

        assertTrue(context.metricSamples.isEmpty())
        assertEquals(2, context.providerErrors.size)
        assertTrue(context.providerErrors.any { it.contains("sentry") })
        assertTrue(context.providerErrors.any { it.contains("humio") })
    }

    @Test
    fun `enrich handles github client failure gracefully`() = runBlocking {
        val registry = FakeTelemetryRegistry()
        val gitHubClient = FakeGitHubClient(GitHubQueryResult.Failure("title", "GitHub 401 Unauthorized"))
        val ldClient = FakeLaunchDarklyClient()
        val jiraClient = FakeJiraClient()

        val enricher = DefaultAlertEnricher(registry, gitHubClient, ldClient, jiraClient)

        val context = enricher.enrich(alert, teamConfig)

        assertTrue(context.recentDeployments.isEmpty())
        assertTrue(context.providerErrors.any { it.contains("GitHub") })
    }

    @Test
    fun `enrich handles launchdarkly client failure gracefully`() = runBlocking {
        val registry = FakeTelemetryRegistry()
        val gitHubClient = FakeGitHubClient()
        val ldClient = FakeLaunchDarklyClient(FlagLookupResult.Failure("sentry", "Flag not found"))
        val jiraClient = FakeJiraClient()

        val enricher = DefaultAlertEnricher(registry, gitHubClient, ldClient, jiraClient)

        val context = enricher.enrich(alert, teamConfig)

        assertTrue(context.activeFeatureFlags.isEmpty())
        assertTrue(context.providerErrors.any { it.contains("LaunchDarkly") })
    }

    @Test
    fun `enrich handles jira client failure gracefully`() = runBlocking {
        val registry = FakeTelemetryRegistry()
        val gitHubClient = FakeGitHubClient()
        val ldClient = FakeLaunchDarklyClient()
        val jiraClient = FakeJiraClient(JiraSearchResult.Failure("PAY", "Jira connection timeout"))

        val enricher = DefaultAlertEnricher(registry, gitHubClient, ldClient, jiraClient)

        val context = enricher.enrich(alert, teamConfig)

        assertTrue(context.relatedJiraTickets.isEmpty())
        assertTrue(context.providerErrors.any { it.contains("Jira") })
    }
}
