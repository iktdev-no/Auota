package no.iktdev.auota.crypt.encrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.common.VerifyOperationBase
import java.nio.file.Path

class VerifyOperationEncrypt(paths: BackendPaths) : VerifyOperationBase(paths) {

    override val log = KotlinLogging.logger {}

    override fun prepareTestFile(): Pair<Path, String> {
        val content = "AUOTA_ENCRYPT_TEST_${System.currentTimeMillis()}"
        return paths.backend.resolve("auota-test.txt") to content
    }

    override val suppressWritable: Boolean = true
}