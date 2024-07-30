package github.buriedincode.kilowog

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempFile
import kotlin.io.path.div
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.pathString

object ZipUtils {
    @JvmStatic
    private val LOGGER = KotlinLogging.logger { }

    fun extractFile(archiveFile: Path, filename: String, extension: String): Path? {
        val tempFile = createTempFile(prefix = "${archiveFile.name}_${filename}_", suffix = ".$extension").toFile()
        tempFile.deleteOnExit()

        val zip = ZipFile(archiveFile.toFile())
        LOGGER.debug { "Extracting $filename.$extension from ${archiveFile.name}" }
        val entry = zip.getEntry("/$filename.$extension") ?: zip.getEntry("$filename.$extension") ?: return null
        zip.getInputStream(entry).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        zip.close()
        return tempFile.toPath()
    }

    fun unzip(archiveFile: Path, destinationFolder: Path) {
        ZipInputStream(archiveFile.inputStream()).use { `in` ->
            var entry: ZipEntry?
            while (`in`.nextEntry.also { entry = it } != null) {
                val filename: String = if (entry!!.name.startsWith("/")) {
                    entry.name.drop(1)
                } else {
                    entry.name
                }
                var destinationFile: Path = destinationFolder / filename
                destinationFile = destinationFile.normalize()
                if (!destinationFile.startsWith(destinationFolder)) {
                    throw RuntimeException("Entry with an illegal path: ${entry!!.name.subSequence(1, entry!!.name.length)}")
                }
                if (entry.isDirectory) {
                    Files.createDirectories(destinationFile)
                } else {
                    Files.createDirectories(destinationFile.parent)
                    Files.copy(`in`, destinationFile)
                }
            }
        }
    }

    fun zip(archiveFile: Path, content: List<Path>) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(archiveFile.toFile()))).use { out ->
            content.map { it.pathString }.forEach { file ->
                FileInputStream(file).use {
                    BufferedInputStream(it).use {
                        val entry = ZipEntry(file.substring(file.lastIndexOf("/")))
                        out.putNextEntry(entry)
                        it.copyTo(out, 1024)
                    }
                }
            }
        }
    }

    fun listFilenames(archiveFile: Path): List<String> = ZipFile(archiveFile.toFile()).stream().map(ZipEntry::getName).toList()
}
