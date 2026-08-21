package com.argus.enrichment.provider

import com.argus.domain.model.AlertContext
import com.argus.domain.model.ProviderKey
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import io.ktor.client.HttpClient

internal class SentryContextProvider(
    private val dsn: String = "",
    private val httpClient: HttpClient? = null,
) : ContextProvider {
    override val key: ProviderKey = ProviderKey.SENTRY

    override suspend fun fetchContext(
        alert: RawAlert,
        teamConfig: TeamConfig,
    ): Result<AlertContext> = Result.success(AlertContext(providerKey = key, items = emptyList()))
}
