package com.argus.test.fakes

import com.argus.enrichment.integrations.github.GitHubClient
import com.argus.enrichment.integrations.github.GitHubQueryResult

class FakeGitHubClient(
    var result: GitHubQueryResult = GitHubQueryResult.Success("default fake github payload"),
) : GitHubClient {
    val recordedQueries = mutableListOf<String>()

    override suspend fun query(query: String): GitHubQueryResult {
        recordedQueries += query
        return result
    }
}
