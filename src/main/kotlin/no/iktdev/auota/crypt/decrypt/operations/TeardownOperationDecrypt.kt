package no.iktdev.auota.crypt.decrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.common.TeardownOperationBase
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths

class TeardownOperationDecrypt(
    runCli: RunCli,
    paths: BackendPaths
) : TeardownOperationBase(runCli, paths) {
    override val log = KotlinLogging.logger {}

}