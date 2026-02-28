package no.iktdev.auota.crypt.common

import mu.KLogger
import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendChecker
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.info.CryptInfoValidator
import no.iktdev.auota.models.EncryptionConfig
import no.iktdev.auota.models.crypt.EncryptionState
import java.nio.file.Files

abstract class AutoInitFlowBase(
    protected val infoValidator: CryptInfoValidator,
    protected val backend: BackendChecker,
    protected val paths: BackendPaths
) {
    abstract val log: KLogger

    protected abstract val initOp: InitOperationBase
    protected abstract val mountOp: MountOperationBase
    protected abstract val verifyOp: VerifyOperationBase

    suspend fun run(cfg: EncryptionConfig): EncryptionState {
        // 1️⃣ Verifiser metadata
        try {
            infoValidator.ensureConsistent()
        } catch (e: Exception) {
            log.error("Metadata mismatch: ${e.message}")
            return EncryptionState.REJECTED
        }

        val configExists = Files.exists(paths.config)

        // 2️⃣ Hvis config mangler men backend har filer → bare mount
        if (!configExists && backend.backendHasFiles()) {
            log.info("Eksisterende data i backend, init mount")
        }

        // 3️⃣ Init backend hvis nødvendig
        if (!configExists && !backend.backendHasFiles()) {
            log.info("Ingen filer i backend, init kryptert backend")
            val ok = initOp.init(cfg)
            if (!ok) return EncryptionState.FAILED
        }

        // 4️⃣ Mount alltid hvis ikke allerede mountet
        if (!backend.isMounted()) {
            log.info("Mounting backend")
            val ok = mountOp.mount(cfg)
            if (!ok) return EncryptionState.FAILED
        }

        // 5️⃣ Verifiser mount
        val verified = verifyOp.verify()
        return if (verified) EncryptionState.READY else EncryptionState.FAILED
    }
}