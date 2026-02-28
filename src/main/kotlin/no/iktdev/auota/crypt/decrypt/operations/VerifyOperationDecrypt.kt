package no.iktdev.auota.crypt.decrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.common.VerifyOperationBase
import java.nio.file.Path

class VerifyOperationDecrypt(paths: BackendPaths) : VerifyOperationBase(paths) {
    override val log = KotlinLogging.logger {}

    override fun prepareTestFile(): Pair<Path, String> {
        val content = "AUOTA_DECRYPT_TEST_${System.currentTimeMillis()}"
        return paths.backend.resolve("auota-test-download.txt") to content
    }

    override val suppressWritable: Boolean = false
}