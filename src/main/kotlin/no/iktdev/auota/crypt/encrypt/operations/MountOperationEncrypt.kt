package no.iktdev.auota.crypt.encrypt.operations

import mu.KotlinLogging
import no.iktdev.auota.crypt.common.MountOperationBase
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import java.nio.file.Path

class MountOperationEncrypt(
    runCli: RunCli,
    paths: BackendPaths,
    configFile: Path
) : MountOperationBase(runCli, paths, configFile) {

    override val log = KotlinLogging.logger {}


    override fun buildMountArgs(passFile: Path): List<String> {
        return listOf(
            "-reverse",
            "-config", configFile.toString(),
            "-passfile", passFile.toString(),
            paths.backend.toString(),
            paths.mount.toString()
        )
    }
}