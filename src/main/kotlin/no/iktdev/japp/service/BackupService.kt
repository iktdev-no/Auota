package no.iktdev.japp.service

import mu.KotlinLogging
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.service.status.JottaStatusService
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class BackupService(
    private val cli: JottaCli,
    private val jottaStatusService: JottaStatusService,
    private val state: BackupStateService
) {
    private val log = KotlinLogging.logger {}

    private fun validatePath(path: String): Path {
        val p = Path.of(path)

        if (!Files.exists(p)) {
            throw IllegalArgumentException("Path does not exist: $path")
        }

        if (!Files.isDirectory(p)) {
            throw IllegalArgumentException("Path is not a directory: $path")
        }

        return p
    }

    suspend fun add(path: String) {
        val validated = validatePath(path)
        state.addRoot(validated.toString())
        syncWithJotta()
    }

    suspend fun remove(path: String) {
        val validated = validatePath(path)
        state.removeRoot(validated.toString())
        syncWithJotta()
    }

    suspend fun syncWithJotta() {
        log.info { "Syncing backup roots with Jotta…" }

        val expected = state.getRoots().toSet()

        val jottaStatus = jottaStatusService.getStatus().parsed
        val actual = jottaStatus
            ?.Backup
            ?.State
            ?.Enabled
            ?.Backups
            ?.mapNotNull { it.Path }
            ?.toSet()
            ?: emptySet()

        val toAdd = expected - actual
        val toRemove = actual - expected

        toAdd.forEach {
            log.info { "Sync: Adding missing root $it" }
            cli.run("add", it)
        }

        toRemove.forEach {
            log.info { "Sync: Removing unexpected root $it" }
            cli.run("rem", it)
        }

        log.info { "Sync complete" }
    }


    suspend fun status() = cli.run("status")
    suspend fun scan() = cli.run("scan")
    suspend fun pause(duration: String? = null) =
        if (duration != null) cli.run("pause", duration) else cli.run("pause")
    suspend fun resume() = cli.run("resume")


}
