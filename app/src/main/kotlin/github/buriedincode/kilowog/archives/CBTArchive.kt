package github.buriedincode.kilowog.archives

import github.buriedincode.kilowog.Utils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.moveTo
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
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Unable to read ${path.name}" }
            emptyList()
        }
    }

    override fun readFile(filename: String): String? {
        if (!listFilenames().contains(filename)) {
            return null
        }
        return try {
            TarArchiveInputStream(FileInputStream(path.toFile())).use { tarInput ->
                generateSequence { tarInput.nextEntry }
                    .first { it.name == filename }
                    .let {
                        ByteArrayOutputStream()
                            .use { output ->
                                IOUtils.copy(tarInput, output)
                                output.toByteArray()
                            }.toString(StandardCharsets.UTF_8)
                    }
            }
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Unable to read $filename from ${path.name}" }
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
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Error extracting files from ${path.name} to $destination" }
            false
        }
    }

    companion object : BaseArchive.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        override fun archiveFiles(src: Path, outputName: String, files: List<Path>?): Path? {
            val outputFilePath = src.parent.resolve("$outputName.cbt")
            return try {
                TarArchiveOutputStream(FileOutputStream(outputFilePath.toFile())).use { tarOutput ->
                    (files ?: Utils.listFiles(path = src)).forEach { file ->
                        val entry = TarArchiveEntry(file.toFile(), src.relativize(file).toString())
                        tarOutput.putArchiveEntry(entry)
                        FileInputStream(file.toFile()).use { input ->
                            IOUtils.copy(input, tarOutput)
                        }
                        tarOutput.closeArchiveEntry()
                    }
                }
                outputFilePath
            } catch (ioe: IOException) {
                LOGGER.error(ioe) { "Error archiving files from $src to $outputFilePath" }
                null
            }
        }

        @OptIn(ExperimentalPathApi::class)
        override fun convert(oldArchive: BaseArchive): BaseArchive? {
            val tempFile = createTempDirectory(prefix = "${oldArchive.path.nameWithoutExtension}_")
            if (!oldArchive.extractFiles(destination = tempFile.toAbsolutePath())) {
                return null
            }
            val archiveFile = archiveFiles(src = tempFile, outputName = "${oldArchive.path.nameWithoutExtension}.cbt") ?: return null
            val newFilepath = oldArchive.path.parent / "${oldArchive.path.nameWithoutExtension}.cbt"
            oldArchive.path.deleteIfExists()
            archiveFile.moveTo(newFilepath)
            tempFile.deleteRecursively()
            return CBTArchive(path = newFilepath)
        }
    }
}
