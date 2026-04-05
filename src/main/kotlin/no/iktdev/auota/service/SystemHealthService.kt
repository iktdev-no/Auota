package no.iktdev.auota.service

import kotlinx.coroutines.flow.MutableStateFlow
import no.iktdev.auota.models.AuthStatus
import no.iktdev.auota.models.SystemHealth
import no.iktdev.auota.service.status.JottaStatusService
import org.springframework.stereotype.Service

@Service
class SystemHealthService(
    private val jottaStatusService: JottaStatusService,
    private val jottad: JottadManager
) {

    val health: MutableStateFlow<SystemHealth> = MutableStateFlow(buildHealth())

    fun buildHealth(): SystemHealth {
        val status = jottaStatusService.cachedStatus.value

        val auth: AuthStatus = when {
            status?.parsed?.User?.AccountInfo != null -> AuthStatus.LOGGED_IN
            status == null -> AuthStatus.LOGGED_OUT
            else -> AuthStatus.UNKNOWN
        }

        return SystemHealth(
            auth = auth,
            jottad = jottad.state.value,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun refresh() {
        health.value = buildHealth()
    }
}
