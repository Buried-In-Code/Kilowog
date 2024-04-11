package github.comiccorps.kilowog.archives

import github.comiccorps.kilowog.Utils
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.Logging
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class CBTArchive(path: Path) : BaseArchive(path = path) {
    override fun listFilenames(): List<String> {
        return try {
            TarArchiveInputStream(FileInputStream(path.toFile())).use { tarInput ->
                generateSequence { tarInput.nextEntry }
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
            TarArchiveInputStream(FileInputStream(path.toFile())).use { tarInput ->
                generateSequence { tarInput.nextEntry }
                    .first { it.name == filename }
                    .let {
                        ByteArrayOutputStream().use { output ->
                            IOUtils.copy(tarInput, output)
                            output.toByteArray()
                        }.toString(StandardCharsets.UTF_8)
                    }
            }
        } catch (e: Exception) {
            logger.error("Unable to read $filename from ${path.name}", e)
            null
        }
    }

    override fun extractFiles(destination: Path): Boolean {
        return try {
            TarArchiveInputStream(FileInputStream(path.toFile())).use { tarInput ->
                generateSequence { tarInput.nextEntry }
                    .forEach { entry ->
                        val entryPath = destination.resolve(entry.name)
                        if (!entry.isDirectory) {
                            Files.createDirectories(entryPath.parent)
                            FileOutputStream(entryPath.toFile()).use { output ->
                                IOUtils.copy(tarInput, output)
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
        override fun archiveFiles(
            src: Path,
            filename: String,
        ): Path? {
            val outputFilePath = src.parent.resolve("$filename.cbt")
            return try {
                TarArchiveOutputStream(FileOutputStream(outputFilePath.toFile())).use { tarOutput ->
                    Utils.listFiles(src).forEach { file ->
                        val entry = TarArchiveEntry(file.toFile(), src.relativize(file).toString())
                        tarOutput.putArchiveEntry(entry)
                        FileInputStream(file.toFile()).use { input ->
                            IOUtils.copy(input, tarOutput)
                        }
                        tarOutput.closeArchiveEntry()
                    }
                }
                outputFilePath
            } catch (e: Exception) {
                logger.error("Error archiving files from $src to $filename.cbt", e)
                null
            }
        }

        @OptIn(ExperimentalPathApi::class)
        override fun convert(old: BaseArchive): BaseArchive? {
            val tempFile = Files.createTempDirectory(old.path.name)
            old.extractFiles(destination = tempFile.toAbsolutePath())
            val newPath = archiveFiles(src = tempFile, filename = "${old.path.nameWithoutExtension}.cbt") ?: return null
            old.path.deleteIfExists()
            tempFile.deleteRecursively()
            return CBTArchive(path = newPath)
        }
    }
}
