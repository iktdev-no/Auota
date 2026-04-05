package no.iktdev.auota.service.status

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.models.JottaDaemonState
import no.iktdev.auota.models.JottaStatus
import no.iktdev.auota.models.JottaSummary
import no.iktdev.auota.service.JottadManager
import no.iktdev.auota.sse.SseHub
import org.springframework.stereotype.Service

@Service
class JottaStatusService(
    private val cli: JottaCli,
    private val sse: SseHub,
    private val jottadManager: JottadManager
) {
    private val log = KotlinLogging.logger {}

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    var cachedStatus: MutableStateFlow<JottaSummary?> = MutableStateFlow(null)

    private var _deviceName: String? = null
    fun getDeviceName(): String? = _deviceName

    init {
        scope.launch {
            jottadManager.state.collect { jotState ->
                handleStateChanges(jotState)
            }
        }
    }

    private fun startPolling() {
        if (pollingJob != null) return

        pollingJob = scope.launch {
            var lastRaw: String? = null

            while (true) {
                val status = getStatus()
                val raw = status.raw

                if (raw != lastRaw) {
                    lastRaw = raw
                    cachedStatus.value = status
                    sse.sendEnvelope("status.jotta", status)
                }

                delay(5000)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun handleStateChanges(
        jotState: JottaDaemonState
    ) {
        log.info("State changed: jottad=$jotState")

        // Instant push
        val status = getStatus()
        cachedStatus.value = status
        sse.sendEnvelope("status.jotta", status)

        // Polling kun når jottad kjører
        if (jotState != JottaDaemonState.RUNNING) {
            stopPolling()
            return
        }

        startPolling()
    }

    suspend fun getStatus(): JottaSummary {
        val jot = jottadManager.state.value

        // JOTTAD GATE
        if (jot != JottaDaemonState.RUNNING) {
            return JottaSummary(
                success = false,
                raw = "",
                parsed = null,
                message = "Jottad not ready ($jot)"
            )
        }

        // JOTTA CLI STATUS
        val result = cli.run("status", "--json")
        val raw = result.output.trim()

        val json = getJsonStatus(raw)
        _deviceName = json?.User?.device?.Name

        if (json != null) {
            return JottaSummary(
                success = true,
                raw = raw,
                parsed = json,
                message = null
            )
        }

        val message: String = when {
            raw.contains("Not logged in", ignoreCase = true) -> "Not logged in"
            raw.contains("Could not connect", ignoreCase = true) -> "Jottad is not running"
            raw.contains("Device not found", ignoreCase = true) -> "Device not registered"
            raw.contains("Permission denied", ignoreCase = true) -> "Permission denied"
            raw.contains("mount", ignoreCase = true) -> "Mount error"
            else -> "Unknown error"
        }

        return JottaSummary(
            success = false,
            raw = raw,
            parsed = null,
            message = message
        )
    }

    fun getJsonStatus(raw: String): JottaStatus? {
        return try {
            Gson().fromJson(raw, JottaStatus::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
