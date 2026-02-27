package no.iktdev.auota.crypt.encrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendChecker
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.info.EncryptionInfoValidator
import no.iktdev.auota.models.EncryptionConfig
import no.iktdev.auota.models.crypt.EncryptionState
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
        // Verifiser metadata
        try {
            infoValidator.ensureConsistent()
        } catch (e: Exception) {
            log.error("Metadata mismatch: ${e.message}")
            return EncryptionState.REJECTED
        }

        val configExists = Files.exists(paths.config)

        // Hvis config mangler men backend har filer → continue
        if (!configExists && backend.backendHasFiles()) {
            log.info("Eksisterende data i backend, initialiserer FUSE reverse mount")
        }

        // Init backend hvis nødvendig (reverse init trengs kun hvis backend tom og config mangler)
        if (!configExists && !backend.backendHasFiles()) {
            log.info("Ingen filer i backend, init kryptert backend")
            val ok = initOp.init(cfg)
            if (!ok) return EncryptionState.FAILED
        }

        // Mount alltid
        if (!backend.isMounted()) {
            log.info("Mounting backend")
            val ok = mountOp.mount(cfg)
            if (!ok) return EncryptionState.FAILED
        }

        val verified = verifyOp.verify()
        return if (verified) EncryptionState.READY else EncryptionState.FAILED
    }
}