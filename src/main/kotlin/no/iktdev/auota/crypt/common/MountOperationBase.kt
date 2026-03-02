package no.iktdev.auota.crypt.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogger
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.models.CryptConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class MountOperationBase(
    protected val runCli: RunCli,
    protected val paths: BackendPaths,
    protected val configFile: Path
) {

    abstract val log: KLogger

    /** Precheck – aldri kritisk, kun info */
    suspend fun precheckMount(path: Path) = withContext(Dispatchers.IO) {
        val absPath = path.toAbsolutePath().toString()
        log.info { "Precheck mount: $absPath" }

        log.info { "Mount-folder eksisterer: ${Files.exists(path)}" }
        val storeType = try { Files.getFileStore(path).type().lowercase() } catch (_: Exception) { "unknown" }
        log.info { "FileStore type: $storeType" }

        val mounted = try {
            Files.readAllLines(Paths.get("/proc/self/mounts")).any { it.split(" ").getOrNull(1) == absPath }
        } catch (_: Exception) { false }
        log.info { "Mount entry funnet: $mounted" }
    }

    /** Full verify etter mount – kritiske sjekker, skrivetest suppresset ved forventet read-only */
    suspend fun verifyMount(path: Path, suppressWritable: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val absPath = path.toAbsolutePath().toString()
        log.info { "Verifisering mount: $absPath" }

        // FileStore check
        val fileStoreOk = try {
            val storeType = Files.getFileStore(path).type().lowercase()
            val looksLikeFuse = storeType.contains("fuse")
            log.info { "FileStore: $storeType (FUSE=${looksLikeFuse})" }
            looksLikeFuse
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke lese FileStore" }
            false
        }

        // /proc/self/mounts check
        val mountsOk = try {
            val found = Files.readAllLines(Paths.get("/proc/self/mounts")).any { it.split(" ").getOrNull(1) == absPath }
            log.info { "Mount entry funnet i /proc/self/mounts: $found" }
            found
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke lese /proc/self/mounts" }
            false
        }

        // Skrivetest – kun info
        val writableOk = try {
            val testFile = path.resolve(".mount-test-${System.currentTimeMillis()}")
            Files.writeString(testFile, "test")
            Files.deleteIfExists(testFile)
            if (!suppressWritable) log.info { "Skrivetest OK" }
            true
        } catch (e: Exception) {
            if (!suppressWritable) log.info { "Skrivetest feilet – mulig read-only: ${e.message}" }
            false
        }

        val criticalOk = fileStoreOk && mountsOk
        if (!criticalOk) log.error { "Kritisk mount-verifisering feilet" }
        criticalOk
    }

    /** Mount backend med gocryptfs – subclass bestemmer args */
    suspend fun mount(cfg: CryptConfig, suppressWritable: Boolean = false): Boolean {
        log.info { "Starter mount-prosess..." }
        log.info { "Backend: ${paths.backend}, Mountpoint: ${paths.mount}" }

        if (cfg.password.isNullOrBlank()) {
            log.error { "Passord mangler" }
            return false
        }

        precheckMount(paths.mount)

        log.info { "Forsøker å rydde ghost mount..." }
        runCli.runCommand("fusermount", listOf("-u", paths.mount.toString()))

        try {
            withContext(Dispatchers.IO) { if (!Files.exists(paths.mount)) Files.createDirectories(paths.mount) }
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke opprette mount directory" }
            return false
        }

        val passFile = try {
            withContext(Dispatchers.IO) {
                val f = Files.createTempFile("gocryptfs-pass", ".txt")
                Files.writeString(f, cfg.password)
                f
            }
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke lage passordfil" }
            return false
        }

        val args = buildMountArgs(passFile)
        log.info { "Kjører gocryptfs med args: $args" }

        val result = runCli.runCommand("gocryptfs", args)
        passFile.toFile().delete()

        if (result.resultCode != 0) {
            log.error { "Mount feilet: exit=${result.resultCode}" }
            return false
        }

        val mounted = verifyMount(paths.mount, suppressWritable)
        if (!mounted) {
            log.error { "Mount startet, men verifisering feilet" }
            return false
        }

        log.info { "Mount OK" }
        return true
    }

    /** Subclass må implementere argumentene til gocryptfs mount */
    protected abstract fun buildMountArgs(passFile: Path): List<String>
}