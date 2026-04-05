package no.iktdev.auota

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.service.JottadManager
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JottadStartup(
    private val jottadManager: JottadManager,
    private val appScope: CoroutineScope
) : ApplicationRunner {

    private val log = KotlinLogging.logger {}

    override fun run(args: ApplicationArguments?) {
        appScope.launch {
            log.info("Kryptering deaktivert → starter jottad umiddelbart...")
            jottadManager.start()
        }
    }

}

