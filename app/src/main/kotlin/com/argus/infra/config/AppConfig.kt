package com.argus.infra.config

import io.ktor.server.config.*

internal data class AppConfig(
    val ollamaHost: String,
    val ollamaModel: String,
    val slackBotToken: String,
    val githubToken: String,
    val launchDarklyToken: String,
    val jiraBaseUrl: String,
    val jiraToken: String,
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): AppConfig =
            AppConfig(
                ollamaHost = config.propertyOrNull("ollama.host")?.getString() ?: "http://localhost:11434",
                ollamaModel = config.propertyOrNull("ollama.model")?.getString() ?: "gpt-oss:20b",
                slackBotToken = config.propertyOrNull("slack.botToken")?.getString() ?: "",
                githubToken = config.propertyOrNull("github.token")?.getString() ?: "",
                launchDarklyToken = config.propertyOrNull("launchdarkly.token")?.getString() ?: "",
                jiraBaseUrl = config.propertyOrNull("jira.baseUrl")?.getString() ?: "",
                jiraToken = config.propertyOrNull("jira.token")?.getString() ?: "",
            )
    }
}
