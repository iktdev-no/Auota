package no.iktdev.japp.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import no.iktdev.japp.models.EncryptionConfig
import no.iktdev.japp.models.EncryptionStatus
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class EncryptionManager {
    val log = KotlinLogging.logger {}


    private val mapper = jacksonObjectMapper()

    private val configFile: Path = Paths.get("/config/encryption.json")
    private val backendPath: Path = Paths.get("/crypt-backend")
    private val mountPath: Path = Paths.get("/crypt")

    fun loadConfig(): EncryptionConfig {
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

    fun isMounted(): Boolean {
        return Files.isDirectory(mountPath) &&
                Files.list(mountPath).use { it.findAny().isPresent }
    }

    fun backendExists(): Boolean {
        return Files.isDirectory(backendPath) &&
                Files.list(backendPath).use { it.findAny().isPresent }
    }

    fun init(cfg: EncryptionConfig): Boolean {
        if (cfg.password.isNullOrBlank()) return false

        Files.createDirectories(backendPath)

        val pb = ProcessBuilder(
            "gocryptfs",
            "-init",
            backendPath.toString()
        )
            .redirectErrorStream(true)
            .apply {
                environment()["GOCRYPTFS_PASSWORD"] = cfg.password
            }

        val p = pb.start()
        return p.waitFor() == 0
    }

    fun mount(cfg: EncryptionConfig): Boolean {
        if (cfg.password.isNullOrBlank()) return false

        Files.createDirectories(mountPath)

        val pb = ProcessBuilder(
            "gocryptfs",
            backendPath.toString(),
            mountPath.toString()
        )
            .redirectErrorStream(true)
            .apply {
                environment()["GOCRYPTFS_PASSWORD"] = cfg.password
            }

        val p = pb.start()
        return p.waitFor() == 0
    }

    fun unmount(): Boolean {
        val p = ProcessBuilder(
            "fusermount",
            "-u",
            mountPath.toString()
        )
            .redirectErrorStream(true)
            .start()

        return p.waitFor() == 0
    }

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

    /**
     * AUTO‑START LOGIKK
     */
    @PostConstruct
    fun autoStart() {
        val cfg = loadConfig()

        if (!cfg.enabled) {
            log.info("Encryption disabled in config")
            return
        }

        log.info("Encryption enabled, checking backend...")

        // 1. Init if backend missing
        if (!backendExists()) {
            log.warn("Encrypted backend missing → initializing new encrypted backend...")
            if (!init(cfg)) {
                log.error("Failed to initialize encrypted backend")
                return
            }
            log.info("Encrypted backend initialized successfully")
        }

        // 2. Mount if not mounted
        if (!isMounted()) {
            log.warn("Encrypted backend not mounted → mounting...")
            if (!mount(cfg)) {
                log.error("Failed to mount encrypted backend")
                return
            }
            log.info("Encrypted backend mounted successfully")
        }

        log.info("Encryption ready: encrypted backend is mounted and active")
    }

}
