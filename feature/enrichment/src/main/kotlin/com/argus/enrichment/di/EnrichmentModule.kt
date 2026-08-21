package com.argus.enrichment.di

import com.argus.enrichment.integrations.github.GitHubClient
import com.argus.enrichment.integrations.github.GitHubClientImpl
import com.argus.enrichment.integrations.jira.JiraClient
import com.argus.enrichment.integrations.jira.JiraClientImpl
import com.argus.enrichment.integrations.launchdarkly.LaunchDarklyClient
import com.argus.enrichment.integrations.launchdarkly.LaunchDarklyClientImpl
import com.argus.enrichment.provider.ContextProvider
import com.argus.enrichment.provider.GitHubContextProvider
import com.argus.enrichment.provider.JiraContextProvider
import com.argus.enrichment.provider.LaunchDarklyContextProvider
import com.argus.enrichment.provider.TelemetryContextProvider
import com.argus.enrichment.service.AlertEnricher
import com.argus.enrichment.service.ConsoleLoggingAlertEnricher
import com.argus.enrichment.service.DefaultAlertEnricher
import com.argus.enrichment.telemetry.FirebaseProvider
import com.argus.enrichment.telemetry.HumioProvider
import com.argus.enrichment.telemetry.InMemoryTelemetryRegistry
import com.argus.enrichment.telemetry.SentryProvider
import com.argus.enrichment.telemetry.TelemetryRegistry
import org.koin.core.module.Module
import org.koin.dsl.module

public val enrichmentModule: Module = enrichmentModule()

public fun enrichmentModule(
    gitHubToken: () -> String = { "" },
    launchDarklyToken: () -> String = { "" },
    jiraBaseUrl: () -> String = { "" },
    jiraToken: () -> String = { "" },
): Module =
    module {
        single<TelemetryRegistry> { InMemoryTelemetryRegistry() }
        single<GitHubClient> { GitHubClientImpl(gitHubToken()) }
        single<LaunchDarklyClient> { LaunchDarklyClientImpl(launchDarklyToken()) }
        single<JiraClient> { JiraClientImpl(jiraBaseUrl(), jiraToken()) }

        single<List<ContextProvider>> {
            listOf(
                TelemetryContextProvider(get()),
                GitHubContextProvider(get()),
                LaunchDarklyContextProvider(get()),
                JiraContextProvider(get()),
            )
        }

        single<AlertEnricher> { DefaultAlertEnricher(get()) }
    }

public val consoleEnrichmentModule: Module =
    module {
        single<TelemetryRegistry> { InMemoryTelemetryRegistry() }
        single<GitHubClient> { GitHubClientImpl() }
        single<LaunchDarklyClient> { LaunchDarklyClientImpl() }
        single<JiraClient> { JiraClientImpl() }
        single<AlertEnricher> { ConsoleLoggingAlertEnricher() }
    }

public fun TelemetryRegistry.registerBuiltInProviders() {
    register(HumioProvider.KEY) { HumioProvider() }
    register(FirebaseProvider.KEY) { FirebaseProvider() }
    register(SentryProvider.KEY) { SentryProvider() }
}
