package no.iktdev.japp.backup

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import no.iktdev.japp.models.backup.BackupItem
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class BackupConfig(
    val roots: List<String> = emptyList(),
    val excluded: List<BackupItem> = emptyList()
)

@Component
class BackupConfigStore {
    private val log = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    private val configFile: Path = Paths.get("/config/backup.json")

    fun load(): BackupConfig {
        return try {
            if (!Files.exists(configFile)) {
                BackupConfig()
            } else {
                mapper.readValue(configFile.toFile())
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to load backup config" }
            BackupConfig()
        }
    }

    fun save(cfg: BackupConfig) {
        try {
            Files.createDirectories(configFile.parent)
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile.toFile(), cfg)
        } catch (e: Exception) {
            log.error(e) { "Failed to save backup config" }
        }
    }
}
