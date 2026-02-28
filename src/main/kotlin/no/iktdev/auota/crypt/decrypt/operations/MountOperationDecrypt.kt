package no.iktdev.auota.crypt.decrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.common.MountOperationBase
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import java.nio.file.Path

class MountOperationDecrypt(
    runCli: RunCli,
    paths: BackendPaths,
    configFile: Path
) : MountOperationBase(runCli, paths, configFile) {

    override val log = KotlinLogging.logger {}

    override fun buildMountArgs(passFile: Path): List<String> {
        return listOf(
            "-config", configFile.toString(),
            "-passfile", passFile.toString(),
            paths.backend.toString(),
            paths.mount.toString()
        )
    }
}