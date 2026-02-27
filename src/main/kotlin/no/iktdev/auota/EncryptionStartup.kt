package no.iktdev.auota

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
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
