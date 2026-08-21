package com.argus.enrichment.telemetry

import java.util.concurrent.ConcurrentHashMap

internal class InMemoryTelemetryRegistry : TelemetryRegistry {
    private val factories = ConcurrentHashMap<String, TelemetryProviderFactory>()

    override fun register(
        key: String,
        factory: TelemetryProviderFactory,
    ) {
        factories[key] = factory
    }

    override fun resolve(key: String): TelemetryProvider {
        val factory = factories[key] ?: throw UnregisteredTelemetryProviderException(key)
        return factory.create()
    }

    override fun isRegistered(key: String): Boolean = factories.containsKey(key)
}
