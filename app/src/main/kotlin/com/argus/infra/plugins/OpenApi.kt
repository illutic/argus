package com.argus.infra.plugins

import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

internal fun Route.configureOpenApi() {
    swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
}
