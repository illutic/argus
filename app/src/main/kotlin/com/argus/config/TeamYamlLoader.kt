package com.argus.config

import com.argus.domain.model.TeamConfig
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.slf4j.LoggerFactory
import java.io.File

internal sealed interface TeamYamlLoadResult {
    data class Success(
        val teamConfig: TeamConfig,
    ) : TeamYamlLoadResult

    data class Failure(
        val path: String,
        val message: String,
    ) : TeamYamlLoadResult
}

/**
 * Loads and deserializes a team YAML profile under `config/teams` into [TeamConfig].
 */
internal class TeamYamlLoader(
    private val yaml: Yaml =
        Yaml(
            configuration =
                YamlConfiguration(
                    strictMode = false,
                ),
        ),
) {
    private val logger = LoggerFactory.getLogger(TeamYamlLoader::class.java)

    fun load(file: File): TeamYamlLoadResult {
        if (!file.exists() || !file.isFile) {
            val msg = "File does not exist: ${file.path}"
            logger.warn(msg)
            return TeamYamlLoadResult.Failure(file.path, msg)
        }

        return runCatching {
            val content = file.readText()
            val config = yaml.decodeFromString(TeamConfig.serializer(), content)
            if (config.teamId.isBlank() || config.jiraPrefix.isBlank() || config.slackChannelId.isBlank()) {
                error("Missing required team configuration fields in ${file.path}")
            }
            logger.info("Successfully loaded team config for teamId={}", config.teamId)
            TeamYamlLoadResult.Success(config)
        }.getOrElse { ex ->
            val errorMsg = "Failed to parse YAML file ${file.path}: ${ex.message}"
            logger.error(errorMsg, ex)
            TeamYamlLoadResult.Failure(file.path, errorMsg)
        }
    }
}
