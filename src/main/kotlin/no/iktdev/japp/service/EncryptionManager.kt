package no.iktdev.japp.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.cli.RunCli
import no.iktdev.japp.models.EncryptionConfig
import no.iktdev.japp.models.EncryptionState
import no.iktdev.japp.models.EncryptionStatus
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class EncryptionManager(
    private val runCli: RunCli
) {
    private val log = KotlinLogging.logger {}

    val state = MutableStateFlow(EncryptionState.NOT_INITIALIZED)

    private val mapper = jacksonObjectMapper()

    private val configFile: Path = Paths.get("/config/encryption.json")
    private val backendPath: Path = Paths.get("/crypt-backend")
    private val mountPath: Path = Paths.get("/crypt")

    // ------------------------------------------------------------
    // CONFIG
    // ------------------------------------------------------------

    fun loadConfig(): EncryptionConfig {
        log.info("Loading config")
        return if (Files.exists(configFile)) {
            mapper.readValue(configFile.toFile())
        } else {
            EncryptionConfig(enabled = false)
        }
    }

    fun saveConfig(cfg: EncryptionConfig) {
        Files.createDirectories(configFile.parent)
        mapper.writerWithDefaultPrettyPrinter().writeValue(configFile.toFile(), cfg)
    }

    // ------------------------------------------------------------
    // STATUS HELPERS
    // ------------------------------------------------------------

    fun isMounted(): Boolean {
        return Files.isDirectory(mountPath) &&
                Files.list(mountPath).use { it.findAny().isPresent }
    }

    fun backendExists(): Boolean {
        return Files.isDirectory(backendPath) &&
                Files.list(backendPath).use { it.findAny().isPresent }
    }

    // ------------------------------------------------------------
    // INIT (non-interactive)
    // ------------------------------------------------------------

    suspend fun init(cfg: EncryptionConfig): Boolean {
        if (cfg.password.isNullOrBlank()) return false

        // Sørg for at backend-mappen finnes
        withContext(Dispatchers.IO) {
            Files.createDirectories(backendPath)
        }

        // Lag passordfil
        val passFile = withContext(Dispatchers.IO) {
            Files.createTempFile("gocryptfs-pass", ".txt").also {
                Files.writeString(it, cfg.password)
            }
        }

        // Kjør gocryptfs -init
        val result = runCli.runCommand(
            executable = "gocryptfs",
            arguments = listOf(
                "-init",
                "-passfile", passFile.toString(),
                backendPath.toString()
            ),
            output = { line -> log.info("[gocryptfs-init] $line") }
        )

        // Slett passordfil
        withContext(Dispatchers.IO) {
            passFile.toFile().delete()
        }

        return result.resultCode == 0
    }

    // ------------------------------------------------------------
    // MOUNT (non-interactive)
    // ------------------------------------------------------------

    suspend fun mount(cfg: EncryptionConfig): Boolean {
        if (cfg.password.isNullOrBlank()) return false

        withContext(Dispatchers.IO) {
            Files.createDirectories(backendPath)
            Files.createDirectories(mountPath)
        }

        val passFile = withContext(Dispatchers.IO) {
            Files.createTempFile("gocryptfs-pass", ".txt").also {
                Files.writeString(it, cfg.password)
            }
        }

        val result = runCli.runCommand(
            executable = "gocryptfs",
            arguments = listOf(
                "-passfile", passFile.toString(),
                backendPath.toString(),
                mountPath.toString()
            ),
            output = { line -> log.info("[gocryptfs-mount] $line") }
        )

        withContext(Dispatchers.IO) {
            passFile.toFile().delete()
        }

        return result.resultCode == 0
    }


    // ------------------------------------------------------------
    // UNMOUNT
    // ------------------------------------------------------------

    suspend fun unmount(): Boolean {
        val result = runCli.runCommand(
            executable = "fusermount",
            arguments = listOf("-u", mountPath.toString()),
            output = { line -> log.info("[fusermount] $line") }
        )

        return result.resultCode == 0
    }

    // ------------------------------------------------------------
    // STATUS
    // ------------------------------------------------------------

    fun getStatus(): EncryptionStatus {
        val cfg = loadConfig()

        return EncryptionStatus(
            enabled = cfg.enabled,
            mounted = isMounted(),
            backendExists = backendExists(),
            algorithm = cfg.algorithm,
            reason = null
        )
    }

    // ------------------------------------------------------------
    // AUTO INIT SEQUENCE
    // ------------------------------------------------------------

    suspend fun autoInitAsync() {
        log.info("Starting encryption manager")
        val cfg = loadConfig()

        // 1. Encryption disabled → done
        if (!cfg.enabled) {
            log.info("Encryption disabled in config")
            state.value = EncryptionState.NOT_ENABLED
            return
        }

        // 2. Start init sequence
        state.value = EncryptionState.INITIALIZING
        log.info("Encryption enabled, checking backend...")

        try {
            // 3. Backend missing → init required
            if (!backendExists()) {
                log.warn("Encrypted backend missing → initializing new encrypted backend...")

                val ok = init(cfg)
                if (!ok) {
                    log.error("Failed to initialize encrypted backend")
                    state.value = EncryptionState.FAILED
                    return
                }

                log.info("Encrypted backend initialized successfully")
            }

            // 4. Backend exists but not mounted → mount required
            if (!isMounted()) {
                log.warn("Encrypted backend not mounted → mounting...")

                val ok = mount(cfg)
                if (!ok) {
                    log.error("Failed to mount encrypted backend")
                    state.value = EncryptionState.FAILED
                    return
                }

                log.info("Encrypted backend mounted successfully")
            }

            // 5. All good
            state.value = EncryptionState.READY
            log.info("Encryption ready: encrypted backend is mounted and active")

        } catch (e: Exception) {
            log.error("Encryption init failed", e)
            state.value = EncryptionState.FAILED
        }
    }
}
