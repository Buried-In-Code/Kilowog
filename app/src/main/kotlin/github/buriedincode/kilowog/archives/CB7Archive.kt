package github.buriedincode.kilowog.archives

import github.buriedincode.kilowog.Utils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.io.IOUtils
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

class CB7Archive(path: Path) : BaseArchive(path = path) {
    private val archive: SevenZFile
        get() = SevenZFile.builder().setPath(path).get()

    override fun listFilenames(): List<String> {
        val output = mutableListOf<String>()
        try {
            archive.use {
                var entry = it.nextEntry
                while (entry != null) {
                    output.add(entry.name)
                    entry = it.nextEntry
                }
            }
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Unable to read ${path.name}" }
        }
        return output
    }

    override fun readFile(filename: String): String? {
        if (!listFilenames().contains(filename)) {
            return null
        }
        return try {
            archive.use {
                it.entries.find { it.name == filename }?.let { entry ->
                    it.getInputStream(entry).use { input ->
                        String(IOUtils.toByteArray(input), StandardCharsets.UTF_8)
                    }
                }
            }
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Unable to read $filename from ${path.name}" }
            null
        }
    }

    override fun extractFiles(destination: Path): Boolean {
        return try {
            archive.use {
                it.entries.forEach { entry ->
                    val entryPath = destination.resolve(entry.name)
                    Files.createDirectories(entryPath.parent)
                    FileOutputStream(entryPath.toFile()).use { output ->
                        IOUtils.copy(archive.getInputStream(entry), output)
                    }
                }
            }
            true
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Error extracting files from ${path.name} to $destination" }
            false
        }
    }

    companion object : BaseArchive.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        override fun archiveFiles(src: Path, outputName: String, files: List<Path>?): Path? {
            val outputFilePath = src.parent.resolve("$outputName.cb7")
            return try {
                SevenZOutputFile(outputFilePath.toFile()).use { archive ->
                    (files ?: Utils.listFiles(path = src)).forEach { file ->
                        val entry = archive.createArchiveEntry(file.toFile(), file.toString())
                        archive.putArchiveEntry(entry)
                        archive.write(Files.readAllBytes(file.toAbsolutePath()))
                        archive.closeArchiveEntry()
                    }
                    archive.finish()
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
            val archiveFile = archiveFiles(src = tempFile, outputName = "${oldArchive.path.nameWithoutExtension}.cb7") ?: return null
            val newFilepath = oldArchive.path.parent / "${oldArchive.path.nameWithoutExtension}.cb7"
            oldArchive.path.deleteIfExists()
            archiveFile.moveTo(newFilepath)
            tempFile.deleteRecursively()
            return CB7Archive(path = newFilepath)
        }
    }
}
