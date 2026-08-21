package com.argus.test.fakes

import com.argus.enrichment.integrations.jira.JiraClient
import com.argus.enrichment.integrations.jira.JiraSearchResult

class FakeJiraClient(
    var result: JiraSearchResult = JiraSearchResult.Success(emptyList()),
) : JiraClient {
    val recordedJqls = mutableListOf<String>()

    override suspend fun search(jql: String): JiraSearchResult {
        recordedJqls += jql
        return result
    }
}
