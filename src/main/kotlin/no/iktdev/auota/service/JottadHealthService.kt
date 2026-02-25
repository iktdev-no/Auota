package no.iktdev.auota.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.models.JottaDaemonState
import org.springframework.stereotype.Service

@Service
class JottadHealthService(
    private val manager: JottadManager,
    private val monitor: JottadMonitor,
    private val appScope: CoroutineScope
) {

    private val log = KotlinLogging.logger {}

    init {
        appScope.launch {
            while (true) {
                delay(2000)

                val running = monitor.isRunning()

                // ❌ Ikke sett RUNNING her
                // RUNNING skal kun settes av JottadManager når loggfilen sier "startup => ok"

                if (!running && manager.state.value == JottaDaemonState.RUNNING) {
                    log.warn("JottadHealth: jottad died → STOPPED")
                    manager.state.value = JottaDaemonState.STOPPED
                }
            }
        }
    }
}
