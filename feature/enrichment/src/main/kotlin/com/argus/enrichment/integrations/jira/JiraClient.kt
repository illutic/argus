package com.argus.enrichment.integrations.jira

sealed interface JiraSearchResult {
    data class Success(
        val issueKeys: List<String>,
    ) : JiraSearchResult

    data class NotImplemented(
        val jql: String,
    ) : JiraSearchResult

    data class Failure(
        val jql: String,
        val message: String,
    ) : JiraSearchResult
}

interface JiraClient {
    suspend fun search(jql: String): JiraSearchResult
}
