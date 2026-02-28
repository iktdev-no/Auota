package no.iktdev.auota.crypt.decrypt

import mu.KotlinLogging
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendChecker
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.backend.BackendReset
import no.iktdev.auota.crypt.common.AbstractCryptManager
import no.iktdev.auota.crypt.common.ConfigExportOperation
import no.iktdev.auota.crypt.common.ConfigImportOperation
import no.iktdev.auota.crypt.decrypt.operations.DecryptAutoInitFlow
import no.iktdev.auota.crypt.decrypt.operations.InitOperationDecrypt
import no.iktdev.auota.crypt.decrypt.operations.MountOperationDecrypt
import no.iktdev.auota.crypt.decrypt.operations.TeardownOperationDecrypt
import no.iktdev.auota.crypt.decrypt.operations.VerifyOperationDecrypt
import no.iktdev.auota.crypt.info.CryptInfoStore
import no.iktdev.auota.crypt.info.CryptInfoValidator
import no.iktdev.auota.models.EncryptionStatus
import no.iktdev.auota.sse.SseHub
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class DecryptionManager(
    override val runCli: RunCli,
    override val sseHub: SseHub
) : AbstractCryptManager(runCli, sseHub) {

    private val log = KotlinLogging.logger {}

    override val configFile = Paths.get("/config/encryption.json")
    override val infoFile = Paths.get("/config/encryption-info.json")

    val encryptedDataPath = Paths.get("/download-encrypted")
    override val dataPath = Paths.get("/download")

    override val backendInfoFile = encryptedDataPath.resolve(".auota-info.json")

    override val paths = BackendPaths(
        backend = encryptedDataPath,  // ← KRYPTERT
        mount = dataPath,             // ← KLARTEXT
        config = gocryptfsConfigPath,
        configEncryptionInfo = infoFile,
        backendInfo = backendInfoFile
    )

    override val infoStore = CryptInfoStore(mapper, infoFile, backendInfoFile)
    override val infoValidator = CryptInfoValidator(infoStore)
    override val backendChecker = BackendChecker(paths)
    override val backendReset = BackendReset(paths)

    override val initOp = InitOperationDecrypt(runCli, paths, infoStore, configDir)
    override val mountOp = MountOperationDecrypt(runCli, paths, gocryptfsConfigPath)
    override val verifyOp = VerifyOperationDecrypt(paths)
    override val teardownOp = TeardownOperationDecrypt(runCli, paths)
    override val configExportOp = ConfigExportOperation(paths)
    override val configImportOp = ConfigImportOperation(paths)

    override val autoInitFlow = DecryptAutoInitFlow(
        infoValidator = infoValidator,
        backend = backendChecker,
        initOp = initOp,
        mountOp = mountOp,
        verifyOp = verifyOp,
        paths = paths
    )

    override fun getStatus(): EncryptionStatus {
        val cfg = loadConfig()
        return EncryptionStatus(
            state = state.value,
            verified = verified.value,
            enabled = cfg.enabled,
            mounted = backendChecker.isMounted(),
            manualOverride = false,
            backendExists = backendChecker.backendExists(),
            algorithm = cfg.algorithm,
            passwordSet = !cfg.password.isNullOrBlank(),
            passwordIncorrect = false,
            reason = null,
            exportable = Files.exists(gocryptfsConfigPath)
        )
    }
}