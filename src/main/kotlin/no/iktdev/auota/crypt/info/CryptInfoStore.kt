package no.iktdev.auota.crypt.info

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path

class CryptInfoStore(
    private val mapper: ObjectMapper,
    private val configInfoPath: Path,
    private val backendInfoPath: Path
) {
    fun loadConfigInfo(): CryptInfo? = read(configInfoPath)
    fun loadBackendInfo(): CryptInfo? = read(backendInfoPath)

    fun saveConfigInfo(info: CryptInfo) = write(configInfoPath, info)
    fun saveBackendInfo(info: CryptInfo) {
    //disabled for now
    //write(backendInfoPath, info)
    }

    private fun read(path: Path): CryptInfo? =
        if (Files.exists(path)) mapper.readValue(path.toFile(), CryptInfo::class.java) else null

    private fun write(path: Path, info: CryptInfo) {
        Files.createDirectories(path.parent)
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), info)
    }
}
