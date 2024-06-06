package github.comiccorps.kilowog.archives

import github.comiccorps.kilowog.Utils
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.Logging
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class CBZArchive(path: Path) : BaseArchive(path = path) {
    private val archive: ZipFile
        get() = ZipFile.builder().setPath(path).get()

    override fun listFilenames(): List<String> {
        return try {
            ZipArchiveInputStream(FileInputStream(path.toFile())).use { zipInput ->
                generateSequence { zipInput.nextEntry }
                    .map { it.name }
                    .toList()
            }
        } catch (e: Exception) {
            logger.error("Unable to read ${path.name}", e)
            emptyList()
        }
    }

    override fun readFile(filename: String): String? {
        return try {
            ZipArchiveInputStream(FileInputStream(path.toFile())).use { zipInput ->
                generateSequence { zipInput.nextEntry }
                    .first { it.name == filename }
                    .let {
                        ByteArrayOutputStream().use { output ->
                            IOUtils.copy(zipInput, output)
                            output.toByteArray()
                        }.decodeToString()
                    }
            }
        } catch (e: Exception) {
            logger.error("Unable to read $filename from ${path.name}", e)
            null
        }
    }

    override fun extractFiles(destination: Path): Boolean {
        return try {
            ZipArchiveInputStream(FileInputStream(path.toFile())).use { zipInput ->
                generateSequence { zipInput.nextEntry }
                    .forEach { entry ->
                        val entryPath = destination.resolve(entry.name)
                        if (!entry.isDirectory) {
                            Files.createDirectories(entryPath.parent)
                            FileOutputStream(entryPath.toFile()).use { output ->
                                IOUtils.copy(zipInput, output)
                            }
                        }
                    }
                true
            }
        } catch (e: Exception) {
            logger.error("Error extracting files from ${path.name} to $destination", e)
            false
        }
    }

    companion object : BaseArchive.Companion(), Logging {
        override fun archiveFiles(src: Path, filename: String): Path? {
            val outputFilePath = src.parent.resolve("$filename.cbz")
            return try {
                ZipArchiveOutputStream(FileOutputStream(outputFilePath.toFile())).use { zipOutput ->
                    Utils.listFiles(src).forEach { file ->
                        val entry = ZipArchiveEntry(src.relativize(file).toString())
                        zipOutput.putArchiveEntry(entry)
                        FileInputStream(file.toFile()).use { input ->
                            IOUtils.copy(input, zipOutput)
                        }
                        zipOutput.closeArchiveEntry()
                    }
                }
                outputFilePath
            } catch (e: Exception) {
                logger.error("Error archiving files from $src to $filename.cbz", e)
                null
            }
        }

        @OptIn(ExperimentalPathApi::class)
        override fun convert(old: BaseArchive): BaseArchive? {
            val tempFile = createTempDirectory(prefix = "${old.path.nameWithoutExtension}_")
            old.extractFiles(destination = tempFile)
            val newPath = archiveFiles(src = tempFile, filename = "${old.path.nameWithoutExtension}.cbz") ?: return null
            old.path.deleteIfExists()
            tempFile.deleteRecursively()
            return CBZArchive(path = newPath)
        }
    }
}
