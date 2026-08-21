package com.argus.ingestion

import com.argus.ingestion.routes.triggerRoutes
import com.argus.ingestion.service.AlertIngestor
import com.argus.test.fakes.FakeAlertIngestor
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TriggerRoutesTest {

    @Test
    fun `POST webhook with valid RawAlert returns 202 and alertId`() = testApplication {
        val fakeIngestor = FakeAlertIngestor(shouldAccept = true)
        val testModule = module {
            single<AlertIngestor> { fakeIngestor }
        }

        application {
            install(Koin) { modules(testModule) }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Invalid payload")))
                }
            }
            routing {
                triggerRoutes()
            }
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.post("/triggers/webhook") {
            contentType(ContentType.Application.Json)
            setBody("""{"teamId":"payments","source":"sentry","title":"DB Connection Error"}""")
        }

        assertEquals(HttpStatusCode.Accepted, response.status)
        assertEquals(1, fakeIngestor.ingestedAlerts.size)
        assertEquals("payments", fakeIngestor.ingestedAlerts.first().teamId)
    }

    @Test
    fun `POST webhook with invalid payload returns 400 Bad Request`() = testApplication {
        val fakeIngestor = FakeAlertIngestor(shouldAccept = false, rejectReason = "teamId cannot be blank")
        val testModule = module {
            single<AlertIngestor> { fakeIngestor }
        }

        application {
            install(Koin) { modules(testModule) }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Invalid payload")))
                }
            }
            routing {
                triggerRoutes()
            }
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.post("/triggers/webhook") {
            contentType(ContentType.Application.Json)
            setBody("""{"teamId":"","source":"sentry","title":"DB Error"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST slack with valid SlackTriggerRequest returns 202 Accepted`() = testApplication {
        val fakeIngestor = FakeAlertIngestor(shouldAccept = true)
        val testModule = module {
            single<AlertIngestor> { fakeIngestor }
        }

        application {
            install(Koin) { modules(testModule) }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Invalid payload")))
                }
            }
            routing {
                triggerRoutes()
            }
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.post("/triggers/slack") {
            contentType(ContentType.Application.Json)
            setBody("""{"teamId":"platform","command":"/investigate","text":"service latency high"}""")
        }

        assertEquals(HttpStatusCode.Accepted, response.status)
        assertEquals(1, fakeIngestor.ingestedAlerts.size)
        val alert = fakeIngestor.ingestedAlerts.first()
        assertEquals("platform", alert.teamId)
        assertEquals("slack", alert.source)
        assertTrue(alert.title.contains("/investigate"))
    }
}
