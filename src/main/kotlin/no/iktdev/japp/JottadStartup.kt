package no.iktdev.japp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.models.EncryptionState
import no.iktdev.japp.service.EncryptionManager
import no.iktdev.japp.service.JottadManager
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JottadStartup(
    private val encryptionManager: EncryptionManager,
    private val jottadManager: JottadManager,
    private val appScope: CoroutineScope
) : ApplicationRunner {

    private val log = KotlinLogging.logger {}

    override fun run(args: ApplicationArguments?) {
        appScope.launch {
            log.info("Venter på encryptering før jottad-start...")

            encryptionManager.state
                .filter { it == EncryptionState.READY || it == EncryptionState.NOT_ENABLED }
                .first()

            log.info("Encryptering OK → starter jottad...")

            jottadManager.start()
        }
    }
}

