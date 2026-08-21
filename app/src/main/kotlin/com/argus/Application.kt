package com.argus

import com.argus.infra.config.AppConfig
import com.argus.infra.di.appModules
import com.argus.infra.plugins.configureMonitoring
import com.argus.infra.plugins.configureOpenApi
import com.argus.infra.plugins.configureSerialization
import com.argus.infra.plugins.configureStatusPages
import com.argus.ingestion.routes.triggerRoutes
import com.argus.routes.healthRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module(koinModules: List<Module> = emptyList()) {
    val appConfig = AppConfig.fromConfig(environment.config)
    log.info("loaded config: ollamaHost={} ollamaModel={}", appConfig.ollamaHost, appConfig.ollamaModel)

    GlobalContext.getOrNull()?.let { GlobalContext.stopKoin() }
    install(Koin) {
        slf4jLogger()
        modules(appModules(appConfig) + koinModules)
    }

    configureSerialization()
    configureStatusPages()
    configureMonitoring()

    routing {
        healthRoutes()
        triggerRoutes()
        configureOpenApi()
    }
}
