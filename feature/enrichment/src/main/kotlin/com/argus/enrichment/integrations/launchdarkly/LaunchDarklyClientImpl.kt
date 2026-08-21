package com.argus.enrichment.integrations.launchdarkly

import io.ktor.client.*

internal class LaunchDarklyClientImpl(
    private val token: String = "",
    private val httpClient: HttpClient? = null,
) : LaunchDarklyClient {
    override suspend fun getFlag(flagKey: String): FlagLookupResult = FlagLookupResult.NotImplemented(flagKey)
}
