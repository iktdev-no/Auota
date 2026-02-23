package no.iktdev.japp.service

import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class JottadMonitor(
    private val manager: JottadManager
) {
    private val log = KotlinLogging.logger {}

    fun isRunning(): Boolean {
        val pid = manager.getPid() ?: return false

        val alive = ProcessHandle.of(pid).map { it.isAlive }.orElse(false)

        if (!alive) {
            log.warn("JottadMonitor: jottad (pid=$pid) is not alive")
        }

        return alive
    }
}
