package no.iktdev.japp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.EncryptionState
import no.iktdev.japp.models.JottaDaemonState
import no.iktdev.japp.service.EncryptionManager
import no.iktdev.japp.service.JottadManager
import no.iktdev.japp.service.StatusService
import no.iktdev.japp.sse.SseHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BackupStartup(
    private val cli: JottaCli,
    private val statusService: StatusService,
    private val sse: SseHub,
    private val encryptionManager: EncryptionManager,
    private val jottadManager: JottadManager,
    private val appScope: CoroutineScope,
    @Value("\${backup.root}") private val backupRoot: String
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

            log.info("BackupStartup: jottad er klar → sjekker backup-status...")

            val status = statusService.getStatus()

            if (!status.success || status.parsed == null) {
                log.error("Kunne ikke hente status: ${status.raw}")
                return@launch
            }

            val backups = status.parsed.Backup
                ?.State
                ?.Enabled
                ?.Backups
                ?: emptyList()

            val alreadyAdded = backups.any { it.Path == backupRoot }

            if (alreadyAdded) {
                log.info("Backup-root '$backupRoot' er allerede registrert.")
                sse.sendEnvelope("bootstrap", mapOf("added" to false))
                return@launch
            }

            log.info("Backup-root mangler – legger til...")

            val addResult = cli.run("add", backupRoot)

            if (addResult.exitCode == 0) {
                log.info("Backup-root ble lagt til OK.")
                sse.sendEnvelope("bootstrap", mapOf("added" to true))
            } else {
                log.error("Klarte ikke å legge til backup-root: ${addResult.output}")
                sse.sendEnvelope("bootstrap", mapOf("error" to addResult.output))
            }
        }
    }
}
