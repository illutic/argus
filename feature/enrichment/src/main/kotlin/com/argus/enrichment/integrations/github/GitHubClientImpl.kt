package com.argus.enrichment.integrations.github

import io.ktor.client.*

internal class GitHubClientImpl(
    private val token: String = "",
    private val httpClient: HttpClient? = null,
) : GitHubClient {
    override suspend fun query(query: String): GitHubQueryResult = GitHubQueryResult.NotImplemented(query)
}
