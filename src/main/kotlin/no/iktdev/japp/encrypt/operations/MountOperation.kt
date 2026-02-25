package no.iktdev.japp.encrypt.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.cli.RunCli
import no.iktdev.japp.encrypt.backend.BackendPaths
import no.iktdev.japp.models.EncryptionConfig
import java.nio.file.Files

class MountOperation(
    private val runCli: RunCli,
    private val paths: BackendPaths
) {
    private val log = KotlinLogging.logger {}

    suspend fun mount(cfg: EncryptionConfig): Boolean {
        log.info { "MountOperation: starter mount-prosess..." }
        log.info { "Backend path: ${paths.backend}" }
        log.info { "Mount path:   ${paths.mount}" }

        if (cfg.password.isNullOrBlank()) {
            log.error { "MountOperation: passord mangler → avbryter" }
            return false
        }

        // Sørg for at mapper finnes
        withContext(Dispatchers.IO) {
            try {
                Files.createDirectories(paths.backend)
                Files.createDirectories(paths.mount)
            } catch (e: Exception) {
                log.error(e) { "MountOperation: klarte ikke å opprette backend/mount-mapper" }
                return@withContext
            }
        }

        // Lag midlertidig passordfil
        val passFile = withContext(Dispatchers.IO) {
            try {
                val file = Files.createTempFile("gocryptfs-pass", ".txt")
                Files.writeString(file, cfg.password)
                log.info { "MountOperation: passordfil opprettet: $file" }
                file
            } catch (e: Exception) {
                log.error(e) { "MountOperation: klarte ikke å lage passordfil" }
                return@withContext null
            }
        } ?: return false

        // Kjør gocryptfs
        val args = listOf(
            "-passfile", passFile.toString(),
            paths.backend.toString(),
            paths.mount.toString()
        )

        log.info { "MountOperation: kjører gocryptfs med args: $args" }

        val result = runCli.runCommand("gocryptfs", args)

        log.info { "MountOperation: gocryptfs exit code: ${result.resultCode}" }
        if (result.output.isNotEmpty()) log.info { "Output:\n${result.output.joinToString("\t\n")}" }

        // Slett passordfil
        try {
            passFile.toFile().delete()
            log.info { "MountOperation: passordfil slettet" }
        } catch (e: Exception) {
            log.warn(e) { "MountOperation: klarte ikke å slette passordfil" }
        }

        if (result.resultCode != 0) {
            log.error { "MountOperation: gocryptfs returnerte feil → mount mislyktes" }
            return false
        }

        // Verifiser at mount faktisk er FUSE
        val store = try {
            withContext(Dispatchers.IO) { Files.getFileStore(paths.mount) }
        } catch (e: Exception) {
            log.error(e) { "MountOperation: klarte ikke å hente FileStore for mount-path" }
            return false
        }

        log.info { "MountOperation: FileStore type: ${store.type()}" }

        val isFuse = store.type().lowercase().contains("fuse")
        if (!isFuse) {
            log.error { "MountOperation: mount ser ikke ut til å være FUSE → mount feilet" }
            return false
        }

        log.info { "MountOperation: mount OK (FUSE bekreftet)" }
        return true
    }
}
