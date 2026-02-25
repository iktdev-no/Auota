package no.iktdev.auota.encrypt.info

import mu.KotlinLogging

class EncryptionInfoValidator(
    private val store: EncryptionInfoStore
) {
    private val log = KotlinLogging.logger {}

    fun ensureConsistent(): EncryptionInfo? {
        val cfg = store.loadConfigInfo()
        val backend = store.loadBackendInfo()

        return when {
            // Full match → OK
            cfg != null && backend != null && cfg == backend -> cfg

            // Config har info, backend mangler → skriv config → backend
            cfg != null && backend == null -> {
                log.warn("Backend missing info → writing config info to backend")
                store.saveBackendInfo(cfg)
                cfg
            }

            // Backend har info, config mangler → mismatch
            cfg == null && backend != null -> {
                log.error("Metadata mismatch: backend has info but config does not")
                throw IllegalStateException("Backend info exists but config info missing")
            }

            // Ingen info ennå → ikke mismatch, bare ikke initialisert
            else -> null
        }
    }
}
