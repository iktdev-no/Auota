package no.iktdev.auota.crypt.decrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendChecker
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.common.AutoInitFlowBase
import no.iktdev.auota.crypt.info.CryptInfoValidator

class DecryptAutoInitFlow(
    infoValidator: CryptInfoValidator,
    backend: BackendChecker,
    paths: BackendPaths,
    override val initOp: InitOperationDecrypt,
    override val mountOp: MountOperationDecrypt,
    override val verifyOp: VerifyOperationDecrypt
) : AutoInitFlowBase(infoValidator, backend, paths) {
    override val log = KotlinLogging.logger {}

}