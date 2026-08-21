package com.argus.enrichment.di

import com.argus.enrichment.provider.ContextProvider
import com.argus.enrichment.provider.FirebaseContextProvider
import com.argus.enrichment.provider.GitHubContextProvider
import com.argus.enrichment.provider.HumioContextProvider
import com.argus.enrichment.provider.JiraContextProvider
import com.argus.enrichment.provider.LaunchDarklyContextProvider
import com.argus.enrichment.provider.SentryContextProvider
import com.argus.enrichment.service.AlertEnricher
import com.argus.enrichment.service.ConsoleLoggingAlertEnricher
import com.argus.enrichment.service.DefaultAlertEnricher
import org.koin.core.module.Module
import org.koin.dsl.module

val enrichmentModule: Module = enrichmentModule()

fun enrichmentModule(
    gitHubToken: () -> String = { "" },
    launchDarklyToken: () -> String = { "" },
    jiraBaseUrl: () -> String = { "" },
    jiraToken: () -> String = { "" },
    humioToken: () -> String = { "" },
    sentryDsn: () -> String = { "" },
    firebaseProjectId: () -> String = { "" },
): Module =
    module {
        single<List<ContextProvider>> {
            listOf(
                GitHubContextProvider(gitHubToken()),
                LaunchDarklyContextProvider(launchDarklyToken()),
                JiraContextProvider(jiraBaseUrl(), jiraToken()),
                HumioContextProvider(humioToken()),
                SentryContextProvider(sentryDsn()),
                FirebaseContextProvider(firebaseProjectId()),
            )
        }

        single<AlertEnricher> { DefaultAlertEnricher(get()) }
    }

val consoleEnrichmentModule: Module =
    module {
        single<AlertEnricher> { ConsoleLoggingAlertEnricher() }
    }
