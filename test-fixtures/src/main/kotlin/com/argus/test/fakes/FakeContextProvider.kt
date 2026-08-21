package com.argus.test.fakes

import com.argus.domain.model.AlertContext
import com.argus.domain.model.ProviderKey
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider
import java.util.concurrent.CopyOnWriteArrayList

class FakeContextProvider(
    override val key: ProviderKey,
    var itemsToReturn: List<String> = emptyList(),
    var errorToThrow: Throwable? = null,
) : ContextProvider {
    val recordedAlerts: MutableList<RawAlert> = CopyOnWriteArrayList()

    override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): Result<AlertContext> {
        recordedAlerts += alert
        return if (errorToThrow != null) {
            Result.failure(errorToThrow!!)
        } else {
            Result.success(AlertContext(providerKey = key, items = itemsToReturn))
        }
    }
}
