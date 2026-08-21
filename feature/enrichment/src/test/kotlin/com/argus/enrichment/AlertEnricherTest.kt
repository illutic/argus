package com.argus.enrichment

import com.argus.domain.model.ProviderKey
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider
import com.argus.enrichment.service.DefaultAlertEnricher
import com.argus.test.fakes.FakeContextProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlertEnricherTest {

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
        val sentryProvider = FakeContextProvider(ProviderKey.SENTRY, listOf("sentry.error_rate: 12.5"))
        val humioProvider = FakeContextProvider(ProviderKey.HUMIO, listOf("humio.p99_latency: 850.0"))
        val githubProvider = FakeContextProvider(ProviderKey.GITHUB, listOf("commit abc1234: update checkout flow"))
        val ldProvider = FakeContextProvider(ProviderKey.LAUNCH_DARKLY, listOf("new-checkout-v2: true"))
        val jiraProvider = FakeContextProvider(ProviderKey.JIRA, listOf("PAY-101: Fix payment timeout"))

        val providers: List<ContextProvider> = listOf(
            sentryProvider,
            humioProvider,
            githubProvider,
            ldProvider,
            jiraProvider,
        )

        val enricher = DefaultAlertEnricher(providers)
        val context = enricher.enrich(alert, teamConfig)

        assertEquals("payments", context.alert.teamId)
        assertEquals(5, context.contexts.size)
        assertTrue(context.providerErrors.isEmpty())

        val sentryContext = context.contexts.first { it.providerKey == ProviderKey.SENTRY }
        assertEquals(1, sentryContext.items.size)
        assertTrue(sentryContext.items.any { it.contains("sentry.error_rate: 12.5") })

        val githubContext = context.contexts.first { it.providerKey == ProviderKey.GITHUB }
        assertTrue(githubContext.items.first().contains("abc1234"))

        val ldContext = context.contexts.first { it.providerKey == ProviderKey.LAUNCH_DARKLY }
        assertTrue(ldContext.items.first().contains("new-checkout-v2: true"))

        val jiraContext = context.contexts.first { it.providerKey == ProviderKey.JIRA }
        assertTrue(jiraContext.items.first().contains("PAY-101"))
    }

    @Test
    fun `enrich handles individual provider failure gracefully without failing entire pipeline`() = runBlocking {
        val failingProvider = FakeContextProvider(
            key = ProviderKey.CUSTOM,
            errorToThrow = IllegalStateException("Network timeout contacting upstream service"),
        )
        val successfulProvider = FakeContextProvider(
            key = ProviderKey.GITHUB,
            itemsToReturn = listOf("healthy diagnostic data"),
        )

        val enricher = DefaultAlertEnricher(listOf(failingProvider, successfulProvider))
        val context = enricher.enrich(alert, teamConfig)

        assertEquals(1, context.contexts.size)
        assertEquals(ProviderKey.GITHUB, context.contexts.first().providerKey)
        assertEquals(1, context.providerErrors.size)
        assertTrue(context.providerErrors.first().contains("CUSTOM"))
        assertTrue(context.providerErrors.first().contains("Network timeout"))
    }
}
