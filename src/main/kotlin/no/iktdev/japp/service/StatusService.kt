package no.iktdev.japp.service

import kotlinx.coroutines.delay
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.StatusResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import no.iktdev.japp.SseHub
import no.iktdev.japp.models.JottaStatus

@Service
class StatusService(
    private val cli: JottaCli,
    private val sse: SseHub,
    private val mapper: ObjectMapper
) {

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
        val result = cli.run("status", "--json")
        val raw = result.output.trim()

        try {
            val parsed = mapper.readValue(raw, JottaStatus::class.java)
            return StatusResponse(true, raw, parsed)
        } catch (_: Exception) {}

        val message = when {
            raw.contains("Not logged in", true) -> "Not logged in"
            raw.contains("Could not connect", true) -> "Jottad is not running"
            raw.contains("Device not found", true) -> "Device not registered"
            else -> "Unknown error"
        }

        return StatusResponse(false, raw, null, message)
    }
}

