package no.iktdev.japp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.japp.encrypt.EncryptionManager
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EncryptionStartup(
    private val encryptionManager: EncryptionManager,
    private val appScope: CoroutineScope
) : ApplicationRunner {

    private val log = KotlinLogging.logger {}

    override fun run(args: ApplicationArguments?) {
        log.info("Starting encryption manager (ApplicationRunner)")
        appScope.launch {
            encryptionManager.autoInitAsync()
        }
    }
}
