package no.iktdev.auota.crypt.encrypt.operations

import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.common.InitOperationBase
import no.iktdev.auota.crypt.info.CryptInfoStore
import java.nio.file.Path

class InitOperationEncrypt(
    runCli: RunCli,
    paths: BackendPaths,
    infoStore: CryptInfoStore,
    configDir: Path
) : InitOperationBase(runCli, paths, infoStore, configDir) {

    override fun buildInitArgs(passFile: Path): List<String> {
        return listOf(
            "-init",
            "-reverse",
            "-plaintextnames",
            "-config", configFile.toString(),
            "-passfile", passFile.toString(),
            paths.backend.toString()
        )
    }
}