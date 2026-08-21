package com.argus.config

import com.argus.domain.model.TeamConfig
import java.io.File

internal sealed interface TeamYamlLoadResult {
    data class Success(
        val teamConfig: TeamConfig,
    ) : TeamYamlLoadResult

    data class NotImplemented(
        val path: String,
    ) : TeamYamlLoadResult

    data class Failure(
        val path: String,
        val message: String,
    ) : TeamYamlLoadResult
}

/**
 * Contract: parse a YAML file under `config/teams` (teamId, jiraPrefix, slackChannelId,
 * repoLayers, telemetry[]) into [TeamConfig]. Body withheld: business logic, out of
 * scope for the scaffold pass.
 */
internal class TeamYamlLoader {
    fun load(file: File): TeamYamlLoadResult =
        TODO(
            "parse ${file.path} into TeamConfig; see class doc for contract",
        )
}
