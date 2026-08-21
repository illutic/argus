package com.argus.infra.plugins

import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Route

internal fun Route.configureOpenApi() {
    swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
}
