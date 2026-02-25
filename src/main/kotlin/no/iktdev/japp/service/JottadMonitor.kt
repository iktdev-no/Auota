package no.iktdev.japp.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

@Service
class JottadMonitor(
    private val manager: JottadManager
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

        return alive
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
