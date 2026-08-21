package com.argus.enrichment.integrations.launchdarkly

sealed interface FlagLookupResult {
    data class Success(
        val flagKey: String,
        val enabled: Boolean,
    ) : FlagLookupResult

    data class NotImplemented(
        val flagKey: String,
    ) : FlagLookupResult

    data class Failure(
        val flagKey: String,
        val message: String,
    ) : FlagLookupResult
}

interface LaunchDarklyClient {
    suspend fun getFlag(flagKey: String): FlagLookupResult
}
