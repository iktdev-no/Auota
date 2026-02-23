package no.iktdev.japp.service

import kotlinx.coroutines.delay
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.StatusResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.japp.models.EncryptionState
import no.iktdev.japp.sse.SseHub
import no.iktdev.japp.models.JottaStatus
import kotlin.math.log

@Service
class StatusService(
    private val cli: JottaCli,
    private val sse: SseHub,
    private val encryptionManager: EncryptionManager
) {
    private val log = KotlinLogging.logger {}

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    init {
        sse.setSubscriberCallbacks(
            onFirst = { startPolling() },
            onNone = { stopPolling() }
        )
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
                    sse.sendEnvelope("status", status)
                }

                delay(5000)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    suspend fun getStatus(): StatusResponse {

        when (encryptionManager.state.value) {
            EncryptionState.NOT_ENABLED -> {
                return StatusResponse(
                    success = false,
                    raw = "",
                    parsed = null,
                    message = "Encryption disabled"
                )
            }

            EncryptionState.NOT_INITIALIZED,
            EncryptionState.INITIALIZING -> {
                return StatusResponse(
                    success = false,
                    raw = "",
                    parsed = null,
                    message = "Encryption not ready"
                )
            }

            EncryptionState.FAILED -> {
                return StatusResponse(
                    success = false,
                    raw = "",
                    parsed = null,
                    message = "Encryption failed"
                )
            }

            EncryptionState.READY -> {
                // fortsett under
            }
        }

        val result = cli.run("status", "--json")
        val raw = result.output.trim()

        // 1) Sjekk om det faktisk ser ut som JSON
        val jsonStatus = getJsonStatus(raw)
        if (jsonStatus != null) {
            return StatusResponse(
                success = true,
                raw = raw,
                parsed = jsonStatus,
                message = null
            )
        } else {
            log.error { "Could not parse status\n$jsonStatus" }

        }

        // 2) Ikke JSON → tolkes som tekstfeil
        val message = when {
            raw.contains("Not logged in", ignoreCase = true) -> "Not logged in"
            raw.contains("Could not connect", ignoreCase = true) -> "Jottad is not running"
            raw.contains("Device not found", ignoreCase = true) -> "Device not registered"
            raw.contains("Permission denied", ignoreCase = true) -> "Permission denied"
            raw.contains("mount", ignoreCase = true) -> "Mount error"
            else -> "Unknown error"
        }

        return StatusResponse(false, raw, null, message)
    }

    fun getJsonStatus(raw: String): JottaStatus? {
        return try {
            Gson().fromJson(raw, JottaStatus::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

