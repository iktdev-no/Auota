package no.iktdev.auota.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.cli.JottaCli.*
import no.iktdev.auota.models.JottaConfig
import org.springframework.stereotype.Service

@Service
class ConfigService(
    private val cli: JottaCli,
    private val mapper: ObjectMapper
) {

    suspend fun getConfig(): JottaConfig {
        return when (val result = cli.run("config", "--json")) {
            is RunResult.Success ->
                mapper.readValue(result.output, JottaConfig::class.java)

            is RunResult.Error ->
                throw IllegalStateException("Failed to read config: ${result.output}")
        }
    }

    suspend fun set(key: String, value: String): Boolean {
        val result = cli.run("config", key, value)

        return result is RunResult.Success
    }
}
