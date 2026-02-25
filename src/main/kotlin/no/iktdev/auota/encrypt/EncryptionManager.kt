package no.iktdev.auota.encrypt

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.encrypt.backend.BackendChecker
import no.iktdev.auota.encrypt.backend.BackendPaths
import no.iktdev.auota.encrypt.backend.BackendReset
import no.iktdev.auota.encrypt.info.EncryptionInfoStore
import no.iktdev.auota.encrypt.info.EncryptionInfoValidator
import no.iktdev.auota.encrypt.operations.*
import no.iktdev.auota.models.*
import no.iktdev.auota.sse.SseHub
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

@Service
class EncryptionManager(
    private val runCli: RunCli,
    private val sseHub: SseHub
) {
    private val log = KotlinLogging.logger {}

    private val manualOverride = AtomicBoolean(false)


    val state = MutableStateFlow(EncryptionState.NOT_INITIALIZED)
    val verified = MutableStateFlow(false)

    private val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()

    private val configFile = Paths.get("/config/encryption.json")
    private val infoFile = Paths.get("/config/encryption-info.json")

    val encryptedDataPath = Paths.get("/dataEncrypted")
    private val backendInfoFile = encryptedDataPath.resolve(".auota-info.json")
    val dataPath = Paths.get("/data")
    private val gocryptfsConfigPath = encryptedDataPath.resolve("gocryptfs.conf")

    private val paths = BackendPaths(
        backend = encryptedDataPath,
        mount = dataPath,
        config = gocryptfsConfigPath,
        configEncryptionInfo = infoFile,
        backendInfo = backendInfoFile,
    )

    private val infoStore = EncryptionInfoStore(mapper, infoFile, backendInfoFile)
    private val infoValidator = EncryptionInfoValidator(infoStore)

    private val backendChecker = BackendChecker(paths)
    private val backendReset = BackendReset(paths)

    private val initOp = InitOperation(runCli, paths, infoStore)
    private val mountOp = MountOperation(runCli, paths)
    private val verifyOp = VerifyOperation(paths)
    private val teardownOp = TeardownOperation(runCli, paths)

    private val configExportOp = ConfigExportOperation(paths)
    private val configImportOp = ConfigImportOperation(paths)


    private val autoInitFlow = AutoInitFlow(
        infoValidator,
        backendChecker,
        initOp,
        mountOp,
        verifyOp,
        paths
    )

    fun enableManualOverride(): Boolean {
        val currentStatus = getStatus()
        if (!currentStatus.enabled || state.value != EncryptionState.READY) {
            return false
        }
        manualOverride.set(true)
        state.value = EncryptionState.MANUAL_OVERRIDE
        sseHub.sendEnvelope("status.encryption", getStatus())
        return true
    }

    fun disableManualOverride() {
        manualOverride.set(false)
        state.value = EncryptionState.NOT_INITIALIZED
        sseHub.sendEnvelope("status.encryption", getStatus())

        GlobalScope.launch { autoInitAsync() }
    }

    suspend fun manualMount(): Boolean {
        enableManualOverride()
        val cfg = loadConfig()
        val ok = mountOp.mount(cfg)
        if (ok) state.value = EncryptionState.READY
        return ok
    }

    suspend fun manualUnmount(): Boolean {
        enableManualOverride()
        val ok = teardownOp.unmount()
        if (ok) state.value = EncryptionState.NOT_INITIALIZED
        return ok
    }


    fun exportGocryptfsConfig(): GocryptfsConfigExport? =
        configExportOp.export()

    fun importGocryptfsConfig(export: GocryptfsConfigExport): Boolean =
        configImportOp.importConfig(export)


    // ------------------------------------------------------------
    // CONFIG
    // ------------------------------------------------------------

    fun loadConfig(): EncryptionConfig {
        return if (Files.exists(configFile)) {
            mapper.readValue(configFile.toFile())
        } else EncryptionConfig(enabled = false)
    }

    fun saveConfig(cfg: EncryptionConfig) {
        Files.createDirectories(configFile.parent)
        mapper.writerWithDefaultPrettyPrinter().writeValue(configFile.toFile(), cfg)
    }

    fun applyConfig(cfg: EncryptionConfig): Boolean {
        return try {
            saveConfig(cfg)

            if (!cfg.enabled) {
                log.info("Encryption disabled → teardown")
                verified.value = false
                state.value = EncryptionState.TEARDOWN

                GlobalScope.launch {
                    if (backendChecker.isMounted()) {
                        teardownOp.unmount()
                    }
                    state.value = EncryptionState.NOT_ENABLED
                }

                return true
            }

            verified.value = false
            state.value = EncryptionState.INITIALIZING

            GlobalScope.launch {
                autoInitAsync()
            }

            true

        } catch (e: Exception) {
            log.error("Failed to apply encryption config", e)
            false
        }
    }

    // ------------------------------------------------------------
    // RESET
    // ------------------------------------------------------------

    suspend fun resetGocryptfsConfig(): Boolean {
        return try {
            log.warn("Resetting gocryptfs config (non-destructive)")

            verified.value = false
            state.value = EncryptionState.NOT_INITIALIZED

            if (backendChecker.isMounted()) {
                teardownOp.unmount()
            }

            backendReset.resetConfig()
        } catch (e: Exception) {
            log.error("Failed to reset gocryptfs config", e)
            false
        }
    }

    // ------------------------------------------------------------
    // STATUS
    // ------------------------------------------------------------

    fun getStatus(): EncryptionStatus {
        val cfg = loadConfig()
        return EncryptionStatus(
            state = state.value,
            verified = verified.value,
            enabled = cfg.enabled,
            mounted = backendChecker.isMounted(),
            manualOverride = manualOverride.get(),
            backendExists = backendChecker.backendExists(),
            algorithm = cfg.algorithm,
            passwordSet = !cfg.password.isNullOrBlank(),
            passwordIncorrect = false,
            reason = null,
            exportable = Files.exists(gocryptfsConfigPath)
        )
    }

    // ------------------------------------------------------------
    // AUTO INIT
    // ------------------------------------------------------------

    suspend fun autoInitAsync() {
        if (manualOverride.get()) {
            log.info("AutoInitFlow skipped due to manual override")
            return
        }

        log.info("Starting encryption manager")
        val cfg = loadConfig()

        if (!cfg.enabled) {
            state.value = EncryptionState.NOT_ENABLED
            return
        }

        try {
            val result = autoInitFlow.run(cfg)
            state.value = result

            if (result == EncryptionState.READY) {
                verified.value = true
            }

        } catch (e: Exception) {
            log.error("Auto-init failed", e)
            state.value = EncryptionState.FAILED
        }
    }

    fun isMounted(): Boolean = backendChecker.isMounted()
    fun backendExists(): Boolean = backendChecker.backendExists()

}
