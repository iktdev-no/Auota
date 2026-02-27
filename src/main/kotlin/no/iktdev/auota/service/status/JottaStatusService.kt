package no.iktdev.auota.service.status

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.crypt.EncryptionState
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
    private val encryptionManager: EncryptionManager,
    private val jottadManager: JottadManager
) {
    private val log = KotlinLogging.logger {}

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    var cachedStatus = MutableStateFlow<JottaSummary?>(null)

    init {
        scope.launch {
            combine(
                encryptionManager.state,
                jottadManager.state
            ) { enc, jot -> enc to jot }
                .collect { (encState, jotState) ->
                    handleStateChanges(encState, jotState)
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
        encState: EncryptionState,
        jotState: JottaDaemonState
    ) {
        log.info("State changed: encryption=$encState, jottad=$jotState")

        // Instant push
        sse.sendEnvelope("status.jotta", getStatus())

        // Polling kun når jottad kjører
        if (jotState != JottaDaemonState.RUNNING) {
            stopPolling()
            return
        }

        startPolling()
    }

    suspend fun getStatus(): JottaSummary {
        val enc = encryptionManager.state.value
        val jot = jottadManager.state.value

        // --- ENCRYPTION GATE ---
        val encryptionHealthy = when (enc) {
            EncryptionState.READY,
            EncryptionState.NOT_ENABLED,
            EncryptionState.NOT_INITIALIZED,
            EncryptionState.MANUAL_OVERRIDE -> true

            else -> false
        }

        // --- JOTTAD GATE ---
        if (jot != JottaDaemonState.RUNNING) {
            return JottaSummary(
                success = encryptionHealthy,
                raw = "",
                parsed = null,
                message = "Jottad not ready ($jot)"
            )
        }

        // --- JOTTA CLI STATUS ---
        val result = cli.run("status", "--json")
        val raw = result.output.trim()

        val json = getJsonStatus(raw)
        if (json != null) {
            return JottaSummary(true, raw, json, null)
        }

        val message = when {
            raw.contains("Not logged in", ignoreCase = true) -> "Not logged in"
            raw.contains("Could not connect", ignoreCase = true) -> "Jottad is not running"
            raw.contains("Device not found", ignoreCase = true) -> "Device not registered"
            raw.contains("Permission denied", ignoreCase = true) -> "Permission denied"
            raw.contains("mount", ignoreCase = true) -> "Mount error"
            else -> "Unknown error"
        }

        return JottaSummary(false, raw, null, message)
    }

    fun getJsonStatus(raw: String): JottaStatus? {
        return try {
            Gson().fromJson(raw, JottaStatus::class.java)
        } catch (e: Exception) {
            null
        }
    }
}