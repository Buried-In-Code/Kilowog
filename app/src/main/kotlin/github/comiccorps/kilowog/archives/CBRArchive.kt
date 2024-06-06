package github.comiccorps.kilowog.archives

import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import org.apache.logging.log4j.kotlin.Logging
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
        } catch (e: RarException) {
            logger.error("Unable to read ${path.name}", e)
            emptyList()
        }
    }

    override fun readFile(filename: String): String? {
        return try {
            archive.use {
                it.fileHeaders.find { it.fileName == filename }?.let { header ->
                    ByteArrayOutputStream().use { output ->
                        it.extractFile(header, output)
                        output.toString(StandardCharsets.UTF_8)
                    }
                }
            }
        } catch (e: RarException) {
            logger.error("Unable to read $filename from ${path.name}", e)
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
        } catch (e: RarException) {
            logger.error("Error extracting files from ${path.name} to $destination", e)
            false
        }
    }

    companion object : BaseArchive.Companion(), Logging {
        override fun archiveFiles(src: Path, filename: String): Path? {
            throw NotImplementedError("Unable to create archive in CBR format")
        }

        override fun convert(old: BaseArchive): BaseArchive? {
            throw NotImplementedError("Unable to change archive to CBR format")
        }
    }
}
