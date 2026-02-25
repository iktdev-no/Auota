package no.iktdev.japp.service.status

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.japp.encrypt.EncryptionManager
import no.iktdev.japp.models.EncryptionState
import no.iktdev.japp.models.EncryptionStatus
import no.iktdev.japp.sse.SseHub
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
