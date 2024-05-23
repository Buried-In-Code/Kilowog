package github.comiccorps.kilowog.archives

import github.comiccorps.kilowog.Utils
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.Logging
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
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
        } catch (e: Exception) {
            logger.error("Unable to read ${path.fileName}", e)
        }
        return output
    }

    override fun readFile(filename: String): String? {
        return try {
            archive.use {
                it.entries.find { it.name == filename }?.let { entry ->
                    it.getInputStream(entry).use { input ->
                        String(IOUtils.toByteArray(input), StandardCharsets.UTF_8)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Unable to read $filename from ${path.fileName}", e)
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
        } catch (e: Exception) {
            logger.error("Error extracting files from ${path.fileName} to $destination", e)
            false
        }
    }

    companion object : BaseArchive.Companion(), Logging {
        override fun archiveFiles(src: Path, filename: String): Path? {
            return try {
                val outputFilePath = src.parent.resolve("$filename.cb7")
                SevenZOutputFile(outputFilePath.toFile()).use { archive ->
                    Utils.listFiles(src).forEach { file ->
                        val entry = archive.createArchiveEntry(file.toFile(), file.toString())
                        archive.putArchiveEntry(entry)
                        archive.write(Files.readAllBytes(file.toAbsolutePath()))
                        archive.closeArchiveEntry()
                    }
                    archive.finish()
                }
                outputFilePath
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                null
            }
        }

        @OptIn(ExperimentalPathApi::class)
        override fun convert(old: BaseArchive): BaseArchive? {
            val tempFile = Files.createTempDirectory(old.path.name)
            old.extractFiles(destination = tempFile.toAbsolutePath())
            val newPath = archiveFiles(src = tempFile, filename = "${old.path.nameWithoutExtension}.cb7") ?: return null
            old.path.deleteIfExists()
            tempFile.deleteRecursively()
            return CB7Archive(path = newPath)
        }
    }
}
