package no.iktdev.auota.service

import mu.KotlinLogging
import no.iktdev.auota.models.JottadStatus
import no.iktdev.auota.sse.SseHub
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class JottadMonitor(
    private val manager: JottadManager,
    private val sseHub: SseHub
) {
    private val log = KotlinLogging.logger {}
    private var deadCount = 0

    fun isRunning(): Boolean {
        val pid = manager.getPid() ?: return false

        val alive = ProcessHandle.of(pid).map { it.isAlive }.orElse(false)

        if (!alive) {
            log.warn("JottadMonitor: jottad (pid=$pid) is not alive")
            deadCount++
        }

        val envilopeStatus = JottadStatus(
            alive = alive,
            state = manager.state.value,
            pid = pid,
            timestamp = System.currentTimeMillis(),
        )
        sseHub.sendEnvelope("status.jottad", envilopeStatus)

        return alive
    }

    fun getJottaDaemonStatus(): JottadStatus {
        val alive = isRunning()
        return JottadStatus(
            alive = alive,
            state = manager.state.value,
            pid = if (alive) manager.getPid()!! else -1,
            timestamp = System.currentTimeMillis(),
        )
    }

    suspend fun checkAndRestartIfNeeded() {
        val isRunning = isRunning()
        if (isRunning) {
            deadCount = 0
            return
        }

        if (deadCount >= 3) {
            log.error { "jottad has been dead for $deadCount checks → restarting daemon" }
            deadCount = 0
            manager.restart()
        }
    }
}

@Service
class JottadScheduler(
    private val monitor: JottadMonitor
) {

    @Scheduled(fixedDelay = 5000)
    suspend fun monitorDaemon() {
        monitor.checkAndRestartIfNeeded()
    }
}
