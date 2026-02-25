package no.iktdev.auota.encrypt.info

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path

class EncryptionInfoStore(
    private val mapper: ObjectMapper,
    private val configInfoPath: Path,
    private val backendInfoPath: Path
) {
    fun loadConfigInfo(): EncryptionInfo? = read(configInfoPath)
    fun loadBackendInfo(): EncryptionInfo? = read(backendInfoPath)

    fun saveConfigInfo(info: EncryptionInfo) = write(configInfoPath, info)
    fun saveBackendInfo(info: EncryptionInfo) = write(backendInfoPath, info)

    private fun read(path: Path): EncryptionInfo? =
        if (Files.exists(path)) mapper.readValue(path.toFile(), EncryptionInfo::class.java) else null

    private fun write(path: Path, info: EncryptionInfo) {
        Files.createDirectories(path.parent)
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), info)
    }
}
