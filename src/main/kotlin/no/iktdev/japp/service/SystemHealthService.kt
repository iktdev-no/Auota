package no.iktdev.japp.service

import kotlinx.coroutines.flow.MutableStateFlow
import no.iktdev.japp.encrypt.EncryptionManager
import no.iktdev.japp.models.AuthStatus
import no.iktdev.japp.models.SystemHealth
import no.iktdev.japp.service.status.JottaStatusService
import org.springframework.stereotype.Service

@Service
class SystemHealthService(
    private val encryption: EncryptionManager,
    private val jottaStatusService: JottaStatusService,
    private val jottad: JottadManager
) {
    val health = MutableStateFlow(buildHealth())

    fun buildHealth(): SystemHealth {
        val status = jottaStatusService.cachedStatus.value

        val auth = when {
            status?.parsed?.User?.AccountInfo != null -> AuthStatus.LOGGED_IN
            status == null -> AuthStatus.LOGGED_OUT
            else -> AuthStatus.UNKNOWN
        }

        return SystemHealth(
            encryption = encryption.state.value,
            auth = auth,
            jottad = jottad.state.value,
            mounted = encryption.isMounted(),
            backendExists = encryption.backendExists(),
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun refresh() {
        health.value = buildHealth()
    }
}
