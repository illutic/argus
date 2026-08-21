package com.argus.enrichment.integrations.github

sealed interface GitHubQueryResult {
    data class Success(
        val payload: String,
    ) : GitHubQueryResult

    data class NotImplemented(
        val query: String,
    ) : GitHubQueryResult

    data class Failure(
        val query: String,
        val message: String,
    ) : GitHubQueryResult
}

interface GitHubClient {
    suspend fun query(query: String): GitHubQueryResult
}
