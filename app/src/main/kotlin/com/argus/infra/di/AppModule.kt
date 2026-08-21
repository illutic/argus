package com.argus.infra.di

import com.argus.alert.di.alertSlackModule
import com.argus.alert.di.consoleAlertModule
import com.argus.analysis.di.analysisModule
import com.argus.analysis.di.consoleAnalysisModule
import com.argus.enrichment.di.consoleEnrichmentModule
import com.argus.enrichment.di.enrichmentModule
import com.argus.infra.config.AppConfig
import com.argus.ingestion.di.consoleIngestionModule
import com.argus.ingestion.di.ingestionModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Standard Production Module Composition
 */
internal fun appModules(appConfig: AppConfig): List<Module> {
    val coreModule =
        module {
            single { appConfig }
            single { HttpClient(CIO) }
        }

    return listOf(
        coreModule,
        ingestionModule,
        enrichmentModule(
            gitHubToken = { appConfig.githubToken },
            launchDarklyToken = { appConfig.launchDarklyToken },
            jiraBaseUrl = { appConfig.jiraBaseUrl },
            jiraToken = { appConfig.jiraToken },
        ),
        analysisModule(
            ollamaHost = { appConfig.ollamaHost },
            ollamaModel = { appConfig.ollamaModel },
        ),
        alertSlackModule(
            botTokenProvider = { appConfig.slackBotToken },
        ),
    )
}

/**
 * Local Development Module Composition (prints to console, zero external credentials needed)
 */
internal fun localAppModules(appConfig: AppConfig): List<Module> {
    val localCoreModule =
        module {
            single { appConfig }
            single { HttpClient(CIO) }
        }

    return listOf(
        localCoreModule,
        consoleIngestionModule,
        consoleEnrichmentModule,
        consoleAnalysisModule,
        consoleAlertModule,
    )
}
