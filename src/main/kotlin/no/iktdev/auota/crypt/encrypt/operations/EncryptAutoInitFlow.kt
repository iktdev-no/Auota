package no.iktdev.auota.crypt.encrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendChecker
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.common.AutoInitFlowBase
import no.iktdev.auota.crypt.info.CryptInfoValidator

class EncryptAutoInitFlow(
    infoValidator: CryptInfoValidator,
    backend: BackendChecker,
    paths: BackendPaths,
    override val initOp: InitOperationEncrypt,
    override val mountOp: MountOperationEncrypt,
    override val verifyOp: VerifyOperationEncrypt
) : AutoInitFlowBase(infoValidator, backend, paths) {
    override val log = KotlinLogging.logger {}

}