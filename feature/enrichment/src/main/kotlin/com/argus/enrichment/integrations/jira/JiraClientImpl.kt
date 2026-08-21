package com.argus.enrichment.integrations.jira

import io.ktor.client.*

internal class JiraClientImpl(
    private val baseUrl: String = "",
    private val token: String = "",
    private val httpClient: HttpClient? = null,
) : JiraClient {
    override suspend fun search(jql: String): JiraSearchResult = JiraSearchResult.NotImplemented(jql)
}
