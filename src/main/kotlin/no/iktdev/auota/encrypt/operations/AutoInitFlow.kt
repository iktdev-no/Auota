package no.iktdev.auota.encrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.encrypt.backend.BackendChecker
import no.iktdev.auota.encrypt.backend.BackendPaths
import no.iktdev.auota.encrypt.info.EncryptionInfoValidator
import no.iktdev.auota.models.EncryptionConfig
import no.iktdev.auota.models.EncryptionState
import java.nio.file.Files

class AutoInitFlow(
    private val infoValidator: EncryptionInfoValidator,
    private val backend: BackendChecker,
    private val initOp: InitOperation,
    private val mountOp: MountOperation,
    private val verifyOp: VerifyOperation,
    private val paths: BackendPaths
) {
    private val log = KotlinLogging.logger {}

    suspend fun run(cfg: EncryptionConfig): EncryptionState {
        val info = try {
            infoValidator.ensureConsistent()
        } catch (e: Exception) {
            log.error("Metadata mismatch: ${e.message}")
            return EncryptionState.REJECTED
        }

        val configExists = Files.exists(paths.config)

        if (!configExists && backend.backendHasFiles()) {
            log.error("Backend contains files but gocryptfs.conf is missing")
            return EncryptionState.REJECTED
        }

        if (!configExists) {
            log.info("Initializing new encrypted backend")
            val ok = initOp.init(cfg)
            if (!ok) {
                log.error("Failed to init backend")
                return EncryptionState.FAILED
            }
        }

        if (!backend.isMounted()) {
            log.info("Mounting backend")
            val ok = mountOp.mount(cfg)
            if (!ok) {
                log.error("Failed to mount backend")
                return EncryptionState.FAILED
            }
        }

        val verified = verifyOp.verify()
        return if (verified) EncryptionState.READY else {
            log.error { "Could not verify backend" }
            EncryptionState.FAILED
        }
    }
}
