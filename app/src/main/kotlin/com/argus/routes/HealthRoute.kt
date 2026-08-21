package com.argus.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Route.healthRoutes() {
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
}
