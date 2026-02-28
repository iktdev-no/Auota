package no.iktdev.auota.crypt.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import java.nio.file.Files
import java.nio.file.Path

abstract class TeardownOperationBase(
    protected val runCli: RunCli,
    protected val paths: BackendPaths
) {
    abstract val log: KLogger

    /** Precheck mount – logg status, men gjør ingen kritiske sjekker */
    suspend fun precheckUnmount(): Boolean = withContext(Dispatchers.IO) {
        val absPath = paths.mount.toAbsolutePath()
        val exists = Files.exists(absPath)
        val mounted = try {
            Files.readAllLines(Path.of("/proc/self/mounts"))
                .any { it.split(" ").getOrNull(1) == absPath.toString() }
        } catch (_: Exception) { false }

        log.info { "Precheck unmount for $absPath – eksisterer: $exists, mountet: $mounted" }
        exists && mounted
    }

    /** Utfør unmount – logg resultat */
    suspend fun unmount(): Boolean = withContext(Dispatchers.IO) {
        log.info { "Forsøker å unmount ${paths.mount}" }
        val result = runCli.runCommand("fusermount", listOf("-u", paths.mount.toString()))
        if (result.resultCode == 0) {
            log.info { "Unmount OK: ${paths.mount}" }
            true
        } else {
            log.warn { "Unmount feilet (exit=${result.resultCode})" }
            false
        }
    }
}