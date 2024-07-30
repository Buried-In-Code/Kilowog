package github.buriedincode.kilowog.archives

import github.buriedincode.kilowog.Utils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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
            ZipArchiveInputStream(FileInputStream(path.toFile())).use { zipInput ->
                generateSequence { zipInput.nextEntry }
                    .first { it.name == filename }
                    .let {
                        ByteArrayOutputStream()
                            .use { output ->
                                IOUtils.copy(zipInput, output)
                                output.toByteArray()
                            }.decodeToString()
                    }
            }
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Unable to read $filename from ${path.name}" }
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
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Error extracting files from ${path.name} to $destination" }
            false
        }
    }

    companion object : BaseArchive.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        override fun archiveFiles(src: Path, outputName: String, files: List<Path>?): Path? {
            val outputFilePath = src.parent.resolve("$outputName.cbz")
            return try {
                ZipArchiveOutputStream(FileOutputStream(outputFilePath.toFile())).use { zipOutput ->
                    (files ?: Utils.listFiles(path = src)).forEach { file ->
                        val entry = ZipArchiveEntry(src.relativize(file).toString())
                        zipOutput.putArchiveEntry(entry)
                        FileInputStream(file.toFile()).use { input ->
                            IOUtils.copy(input, zipOutput)
                        }
                        zipOutput.closeArchiveEntry()
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
            val archiveFile = archiveFiles(src = tempFile, outputName = "${oldArchive.path.nameWithoutExtension}.cbz") ?: return null
            val newFilepath = oldArchive.path.parent / "${oldArchive.path.nameWithoutExtension}.cbz"
            oldArchive.path.deleteIfExists()
            archiveFile.moveTo(newFilepath)
            tempFile.deleteRecursively()
            return CBZArchive(path = newFilepath)
        }
    }
}
