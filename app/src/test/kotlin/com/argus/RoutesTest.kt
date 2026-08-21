package com.argus

import com.argus.alert.sink.AlertSink
import com.argus.test.fakes.FakeAlertSink
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutesTest {
    private val testModule =
        module {
            single<AlertSink> { FakeAlertSink() }
        }

    @Test
    fun `health route returns 200`() =
        testApplication {
            application { module(koinModules = listOf(testModule)) }

            val response = client.get("/health")

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun `webhook trigger route accepts alert with 202`() =
        testApplication {
            application { module(koinModules = listOf(testModule)) }

            val response =
                client.post("/triggers/webhook") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"teamId":"test-team","source":"sentry","title":"High latency"}""")
                }

            assertEquals(HttpStatusCode.Accepted, response.status)
        }

    @Test
    fun `slack trigger route accepts alert with 202`() =
        testApplication {
            application { module(koinModules = listOf(testModule)) }

            val response =
                client.post("/triggers/slack") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"teamId":"test-team","command":"/triage","text":"checkout failing"}""")
                }

            assertEquals(HttpStatusCode.Accepted, response.status)
        }

    @Test
    fun `swagger UI route is reachable`() =
        testApplication {
            application { module(koinModules = listOf(testModule)) }

            val response = client.get("/swagger")

            assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.MovedPermanently)
        }

    @Test
    fun `openapi route is reachable`() =
        testApplication {
            application { module(koinModules = listOf(testModule)) }

            val response = client.get("/openapi")

            assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.MovedPermanently)
        }
}
