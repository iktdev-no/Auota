package no.iktdev.auota.crypt.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.*
import no.iktdev.auota.crypt.info.CryptInfoStore
import no.iktdev.auota.crypt.info.CryptInfoValidator
import no.iktdev.auota.models.*
import no.iktdev.auota.models.crypt.EncryptionState
import no.iktdev.auota.sse.SseHub
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractCryptManager(
    protected open val runCli: RunCli,
    protected open val sseHub: SseHub
) {
    private val log = KotlinLogging.logger {}
    protected val mapper = jacksonObjectMapper()

    abstract val configFile: Path
    abstract val infoFile: Path
    abstract val backendInfoFile: Path

    protected val manualOverride = AtomicBoolean(false)
    val state = MutableStateFlow(EncryptionState.NOT_INITIALIZED)
    val verified = MutableStateFlow(false)

    protected val configDir = Paths.get("/config")
    protected val gocryptfsConfigPath = configDir.resolve("gocryptfs.conf")

    abstract val dataPath: Path
    abstract val paths: BackendPaths

    abstract val infoStore: CryptInfoStore
    abstract val infoValidator: CryptInfoValidator
    abstract val backendChecker: BackendChecker
    abstract val backendReset: BackendReset

    abstract val initOp: InitOperationBase
    abstract val mountOp: MountOperationBase
    abstract val verifyOp: VerifyOperationBase
    abstract val teardownOp: TeardownOperationBase
    abstract val configExportOp: ConfigExportOperation
    abstract val configImportOp: ConfigImportOperation

    protected abstract val autoInitFlow: AutoInitFlowBase

    // ------------------------------------------------------------
    // GOCryptFS EXPORT/IMPORT
    // ------------------------------------------------------------
    fun exportGocryptfsConfig(): GocryptfsConfigExport? = configExportOp.export()
    fun importGocryptfsConfig(export: GocryptfsConfigExport): Boolean = configImportOp.importConfig(export)

    fun isMounted(): Boolean = backendChecker.isMounted()
    fun backendExists(): Boolean = backendChecker.backendExists()

    // ------------------------------------------------------------
    // CONFIG
    // ------------------------------------------------------------
    fun loadConfig(): CryptConfig =
        if (Files.exists(configFile)) mapper.readValue(configFile.toFile()) else CryptConfig(enabled = false)

    fun saveConfig(cfg: CryptConfig) {
        Files.createDirectories(configFile.parent)
        mapper.writerWithDefaultPrettyPrinter().writeValue(configFile.toFile(), cfg)
    }

    fun applyConfig(cfg: CryptConfig): Boolean {
        return try {
            saveConfig(cfg)
            if (!cfg.enabled) {
                log.info("Encryption disabled → teardown")
                verified.value = false
                state.value = EncryptionState.TEARDOWN

                GlobalScope.launch {
                    if (backendChecker.isMounted()) teardownOp.unmount()
                    state.value = EncryptionState.NOT_ENABLED
                }
                return true
            }

            verified.value = false
            state.value = EncryptionState.INITIALIZING
            GlobalScope.launch { autoInitAsync() }
            true
        } catch (e: Exception) {
            log.error("Failed to apply encryption config", e)
            false
        }
    }

    suspend fun resetGocryptfsConfig(): Boolean {
        return try {
            log.warn("Resetting gocryptfs config (non-destructive)")
            verified.value = false
            state.value = EncryptionState.NOT_INITIALIZED
            if (backendChecker.isMounted()) teardownOp.unmount()
            backendReset.resetConfig()
        } catch (e: Exception) {
            log.error("Failed to reset gocryptfs config", e)
            false
        }
    }

    // ------------------------------------------------------------
    // STATUS
    // ------------------------------------------------------------
    abstract fun getStatus(): EncryptionStatus

    // ------------------------------------------------------------
    // MANUAL OVERRIDE
    // ------------------------------------------------------------
    fun enableManualOverride(): Boolean {
        val currentStatus = getStatus()
        if (!currentStatus.enabled || state.value != EncryptionState.READY) return false
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


    // ------------------------------------------------------------
    // AUTO INIT
    // ------------------------------------------------------------
    open suspend fun autoInitAsync() {
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
            if (result == EncryptionState.READY) verified.value = true
        } catch (e: Exception) {
            log.error("Auto-init failed", e)
            state.value = EncryptionState.FAILED
        }
    }


}