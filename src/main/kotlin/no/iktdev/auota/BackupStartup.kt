package no.iktdev.auota

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.encrypt.EncryptionManager
import no.iktdev.auota.models.EncryptionState
import no.iktdev.auota.models.JottaDaemonState
import no.iktdev.auota.service.BackupService
import no.iktdev.auota.service.JottadManager
import no.iktdev.auota.service.status.JottaStatusService
import no.iktdev.auota.sse.SseHub
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BackupStartup(
    private val cli: JottaCli,
    private val jottaStatusService: JottaStatusService,
    private val sse: SseHub,
    private val encryptionManager: EncryptionManager,
    private val jottadManager: JottadManager,
    private val appScope: CoroutineScope,
    private val backupService: BackupService,
) {

    private val log = KotlinLogging.logger {}

    @EventListener(ApplicationReadyEvent::class)
    fun onReady() {
        appScope.launch {
            log.info("BackupStartup: venter på encryption...")

            encryptionManager.state
                .filter { it == EncryptionState.READY || it == EncryptionState.NOT_ENABLED }
                .first()

            log.info("BackupStartup: venter på jottad...")

            jottadManager.state
                .filter { it == JottaDaemonState.RUNNING }
                .first()

            delay(1000)
            log.info("BackupStartup: jottad er klar → sjekker backup-status...")

            val status = jottaStatusService.getStatus()

            if (!status.success || status.parsed == null) {
                log.error("Kunne ikke hente status: ${status.raw}")
                return@launch
            }
            log.info("BackupStartup: status OK → synkroniserer backup-roots...")
            backupService.syncWithJotta()
        }
    }
}
