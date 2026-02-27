package no.iktdev.auota.service.status

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.crypt.EncryptionState
import no.iktdev.auota.models.EncryptionStatus
import no.iktdev.auota.sse.SseHub
import org.springframework.stereotype.Service

@Service
class EncryptionStatusService(
    private val encryptionManager: EncryptionManager,
    private val sse: SseHub
) {
    private val log = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            encryptionManager.state.collect { encState ->
                handleEncryptionState(encState)
            }
        }
    }

    private suspend fun handleEncryptionState(encState: EncryptionState) {
        log.info("Encryption state changed → $encState")
        sse.sendEnvelope("status.encryption", getStatus())
    }

    fun getStatus(): EncryptionStatus {
        val enc = encryptionManager.state.value

       return encryptionManager.getStatus()
    }
}
