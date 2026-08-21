package com.argus.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.healthRoutes() {
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
}
