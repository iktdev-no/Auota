package no.iktdev.auota.encrypt.operations

import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.encrypt.backend.BackendPaths

class TeardownOperation(
    private val runCli: RunCli,
    private val paths: BackendPaths
) {
    suspend fun unmount(): Boolean {
        val result = runCli.runCommand("fusermount", listOf("-u", paths.mount.toString()))
        return result.resultCode == 0
    }
}
