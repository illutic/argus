package com.argus.enrichment

import com.argus.domain.model.MetricSample
import com.argus.enrichment.telemetry.InMemoryTelemetryRegistry
import com.argus.enrichment.telemetry.TelemetryFetchResult
import com.argus.enrichment.telemetry.TelemetryProvider
import com.argus.enrichment.telemetry.UnregisteredTelemetryProviderException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryTelemetryRegistryTest {

    private class TestProvider(override val key: String) : TelemetryProvider {
        override suspend fun fetch(teamId: String): TelemetryFetchResult =
            TelemetryFetchResult.Success(
                listOf(MetricSample(providerKey = key, teamId = teamId, name = "latency", value = 42.0)),
            )
    }

    @Test
    fun `registers and resolves provider factory`() {
        val registry = InMemoryTelemetryRegistry()
        registry.register("humio") { TestProvider("humio") }

        assertTrue(registry.isRegistered("humio"))
        val provider = registry.resolve("humio")
        assertEquals("humio", provider.key)
    }

    @Test
    fun `resolving unregistered provider throws UnregisteredTelemetryProviderException`() {
        val registry = InMemoryTelemetryRegistry()

        assertFalse(registry.isRegistered("nonexistent"))
        assertFailsWith<UnregisteredTelemetryProviderException> {
            registry.resolve("nonexistent")
        }
    }
}
