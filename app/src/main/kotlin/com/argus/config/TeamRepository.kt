package com.argus.config

import com.argus.domain.model.TeamConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository contract for retrieving and storing team configuration profiles.
 */
interface TeamRepository {
    fun get(teamId: String): TeamConfig?

    fun getAll(): List<TeamConfig>

    fun save(teamConfig: TeamConfig)

    fun saveAll(configs: List<TeamConfig>)

    fun clear()
}

/**
 * Thread-safe in-memory storage for team routing configurations.
 */
internal class InMemoryTeamRepository : TeamRepository {
    private val teams = ConcurrentHashMap<String, TeamConfig>()

    override fun get(teamId: String): TeamConfig? = teams[teamId]

    override fun getAll(): List<TeamConfig> = teams.values.toList()

    override fun save(teamConfig: TeamConfig) {
        teams[teamConfig.teamId] = teamConfig
    }

    override fun saveAll(configs: List<TeamConfig>) {
        configs.forEach { save(it) }
    }

    override fun clear() {
        teams.clear()
    }
}
