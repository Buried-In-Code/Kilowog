package github.buriedincode.kilowog.archives

import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.outputStream

class CBRArchive(path: Path) : BaseArchive(path = path) {
    private val archive: Archive
        get() = Archive(path.toFile())

    override fun listFilenames(): List<String> {
        return try {
            archive.use {
                it.fileHeaders.mapNotNull { it.fileName }
            }
        } catch (re: RarException) {
            LOGGER.error(re) { "Unable to read ${path.name}" }
            emptyList()
        }
    }

    override fun readFile(filename: String): String? {
        if (!listFilenames().contains(filename)) {
            return null
        }
        return try {
            archive.use {
                it.fileHeaders.find { it.fileName == filename }?.let { header ->
                    ByteArrayOutputStream().use { output ->
                        it.extractFile(header, output)
                        output.toString(StandardCharsets.UTF_8)
                    }
                }
            }
        } catch (re: RarException) {
            LOGGER.error(re) { "Unable to read $filename from ${path.name}" }
            null
        }
    }

    override fun extractFiles(destination: Path): Boolean {
        return try {
            archive.use {
                it.fileHeaders.forEach { header ->
                    it.extractFile(header, destination.resolve(header.fileName).outputStream())
                }
            }
            true
        } catch (re: RarException) {
            LOGGER.error(re) { "Error extracting files from ${path.name} to $destination" }
            false
        }
    }

    companion object : BaseArchive.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        override fun archiveFiles(src: Path, outputName: String, files: List<Path>?): Path? {
            throw NotImplementedError("Unable to create archive in CBR format")
        }

        override fun convert(oldArchive: BaseArchive): BaseArchive? {
            throw NotImplementedError("Unable to change archive to CBR format")
        }
    }
}
