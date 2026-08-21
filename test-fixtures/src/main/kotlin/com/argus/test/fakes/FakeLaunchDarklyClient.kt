package com.argus.test.fakes

import com.argus.enrichment.integrations.launchdarkly.FlagLookupResult
import com.argus.enrichment.integrations.launchdarkly.LaunchDarklyClient

class FakeLaunchDarklyClient(
    var result: FlagLookupResult = FlagLookupResult.Success(flagKey = "default", enabled = true),
) : LaunchDarklyClient {
    val recordedFlags = mutableListOf<String>()

    override suspend fun getFlag(flagKey: String): FlagLookupResult {
        recordedFlags += flagKey
        return result
    }
}
