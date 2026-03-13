package no.iktdev.auota.crypt.decrypt.operations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendChecker
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.common.AutoInitFlowBase
import no.iktdev.auota.crypt.info.CryptInfoValidator
import no.iktdev.auota.models.CryptConfig
import no.iktdev.auota.service.DecryptedSyncService
import java.nio.file.Path

class DecryptAutoInitFlow(
    val decryptedSyncService: DecryptedSyncService,
    val exportFolder: Path,
    infoValidator: CryptInfoValidator,
    backend: BackendChecker,
    paths: BackendPaths,
    override val initOp: InitOperationDecrypt,
    override val mountOp: MountOperationDecrypt,
    override val verifyOp: VerifyOperationDecrypt,
    val decryptedView: Path,
) : AutoInitFlowBase(infoValidator, backend, paths) {
    override val log = KotlinLogging.logger {}

    override suspend fun afterVerifySuccess(cfg: CryptConfig) {
        super.afterVerifySuccess(cfg)

        CoroutineScope(Dispatchers.IO).launch {
            decryptedSyncService.startPolling(decryptedView, exportFolder)
        }
    }

}