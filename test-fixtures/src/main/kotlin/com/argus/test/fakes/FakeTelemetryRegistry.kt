package com.argus.test.fakes

import com.argus.enrichment.telemetry.TelemetryProvider
import com.argus.enrichment.telemetry.TelemetryProviderFactory
import com.argus.enrichment.telemetry.TelemetryRegistry
import com.argus.enrichment.telemetry.UnregisteredTelemetryProviderException

class FakeTelemetryRegistry : TelemetryRegistry {
    private val factories = mutableMapOf<String, TelemetryProviderFactory>()

    override fun register(
        key: String,
        factory: TelemetryProviderFactory,
    ) {
        factories[key] = factory
    }

    override fun resolve(key: String): TelemetryProvider = (factories[key] ?: throw UnregisteredTelemetryProviderException(key)).create()

    override fun isRegistered(key: String): Boolean = factories.containsKey(key)
}
