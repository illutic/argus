package com.argus.config

import com.argus.domain.model.TeamConfig
import com.argus.enrichment.provider.ContextProvider
import org.slf4j.LoggerFactory
import java.io.File

internal sealed interface TeamConfigSyncResult {
    data class Synced(
        val teamId: String,
    ) : TeamConfigSyncResult

    data class UnregisteredProvider(
        val teamId: String,
        val providerKey: String,
    ) : TeamConfigSyncResult

    data class LoadFailure(
        val path: String,
        val message: String,
    ) : TeamConfigSyncResult
}

/**
 * Validates team configurations against registered [ContextProvider] keys,
 * stores valid team profiles in the in-memory [TeamRepository], and gracefully
 * records warnings for invalid team YAML profiles without halting execution.
 */
internal class TeamConfigSync(
    private val contextProviders: List<ContextProvider>,
    private val teamRepository: TeamRepository,
) {
    private val logger = LoggerFactory.getLogger(TeamConfigSync::class.java)

    fun sync(teamConfig: TeamConfig): TeamConfigSyncResult {
        val registeredKeys =
            contextProviders
                .map {
                    it.key.name
                        .lowercase()
                        .replace("_", "")
                }.toSet() +
                contextProviders.map { it.key.name.lowercase() }.toSet()

        for (provider in teamConfig.telemetry) {
            val normalized = provider.lowercase().replace("-", "").replace("_", "")
            val isKnown = registeredKeys.any { it.replace("-", "").replace("_", "") == normalized }
            if (!isKnown) {
                val errorMsg = "Unregistered telemetry provider '$provider' for team '${teamConfig.teamId}'. Skipping sync for this team."
                logger.warn(errorMsg)
                return TeamConfigSyncResult.UnregisteredProvider(teamConfig.teamId, provider)
            }
        }

        teamRepository.save(teamConfig)
        logger.info("Successfully synced team config into in-memory repository for teamId={}", teamConfig.teamId)
        return TeamConfigSyncResult.Synced(teamConfig.teamId)
    }

    fun syncDirectory(
        directory: File,
        loader: TeamYamlLoader = TeamYamlLoader(),
    ): List<TeamConfigSyncResult> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        val yamlFiles =
            directory
                .listFiles { file ->
                    file.isFile && (file.extension == "yaml" || file.extension == "yml")
                }.orEmpty()

        return yamlFiles.map { file ->
            when (val loadResult = loader.load(file)) {
                is TeamYamlLoadResult.Success -> {
                    sync(loadResult.teamConfig)
                }

                is TeamYamlLoadResult.Failure -> {
                    logger.warn("Skipping invalid team YAML {}: {}", file.path, loadResult.message)
                    TeamConfigSyncResult.LoadFailure(file.path, loadResult.message)
                }
            }
        }
    }
}
