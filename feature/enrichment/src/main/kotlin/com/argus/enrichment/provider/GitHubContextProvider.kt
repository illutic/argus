package com.argus.enrichment.provider

import com.argus.domain.model.AlertContext
import com.argus.domain.model.ProviderKey
import com.argus.domain.model.RawAlert
import com.argus.domain.model.TeamConfig
import io.ktor.client.HttpClient

internal class GitHubContextProvider(
    private val token: String = "",
    private val httpClient: HttpClient? = null,
) : ContextProvider {
    override val key: ProviderKey = ProviderKey.GITHUB

    override suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): Result<AlertContext> {
        return Result.success(AlertContext(providerKey = key, items = emptyList()))
    }
}
