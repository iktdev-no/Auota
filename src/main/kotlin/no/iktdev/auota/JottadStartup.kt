package no.iktdev.auota

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.crypt.EncryptionState
import no.iktdev.auota.service.JottadManager
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

