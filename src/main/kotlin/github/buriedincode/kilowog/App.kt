package github.buriedincode.kilowog

import com.github.junrar.Junrar
import github.buriedincode.kilowog.Utils.isNullOrBlank
import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.ComicInfo
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.MetronInfo
import github.buriedincode.kilowog.services.ComicvineTalker
import github.buriedincode.kilowog.services.MetronTalker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

object App : Logging {
    fun convertCollection(directory: Path) {
        Utils.listFiles(directory, "cbr").forEach { srcFile ->
            Console.print("Converting ${srcFile.name} to CBZ format")
            logger.info("Converting ${srcFile.name} to CBZ format")
            val tempDir = createTempDirectory(srcFile.nameWithoutExtension)

            Junrar.extract(srcFile.toFile(), tempDir.toFile())

            val destFile = Path(srcFile.parent.pathString, srcFile.nameWithoutExtension + ".cbz")
            ZipUtils.zip(destFile = destFile, content = Utils.listFiles(path = tempDir))
            tempDir.toFile().deleteRecursively()
            srcFile.toFile().delete()
        }
    }

    private fun readInfoFile(archiveFile: File, infoFile: String): String? {
        val tempFile = createTempFile(prefix = "${archiveFile.name}_${infoFile}_", suffix = ".xml").toFile()
        tempFile.deleteOnExit()

        if (archiveFile.extension == "cbz") {
            val zip = ZipFile(archiveFile)
            val entry = zip.getEntry("/$infoFile.xml") ?: return null
            zip.getInputStream(entry).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            zip.close()
        } else {
            return null
        }
        return tempFile.readText()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetadata(archiveFile: File): Metadata? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "Metadata")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<Metadata>(content)
        } catch (mfe: MissingFieldException) {
            logger.error("Metadata config is invalid: ${mfe.message}")
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetronInfo(archiveFile: File): MetronInfo? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "MetronInfo")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<MetronInfo>(content)
        } catch (mfe: MissingFieldException) {
            logger.error("MetronInfo config is invalid: ${mfe.message}")
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readComicInfo(archiveFile: File): ComicInfo? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "ComicInfo")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<ComicInfo>(content)
        } catch (mfe: MissingFieldException) {
            logger.error("ComicInfo config is invalid: ${mfe.message}")
            null
        }
    }

    fun readCollection(directory: Path): Map<Path, Metadata?> {
        val files = Utils.listFiles(directory, "cbz")
        return files.associateWith {
            readMetadata(archiveFile = it.toFile())
                ?: readMetronInfo(archiveFile = it.toFile())?.toMetadata()
                ?: readComicInfo(archiveFile = it.toFile())?.toMetadata()
        }
    }

    private fun parsePages(folder: Path, metadata: Metadata, filename: String) {
        val imageList = Utils.listFiles(path = folder).sorted()
        imageList.filterNot { it.extension == "xml" }.forEachIndexed { index, it ->
            val padCount = imageList.size.toString().length
            val newPage = metadata.pages.getOrNull(index = index) == null
            val page = metadata.pages.getOrNull(index = index) ?: Metadata.Page(filename = it.name, index = index)
            val image = ImageIO.read(it.toFile())
            page.doublePage = image.width >= image.height
            page.fileSize = it.fileSize()
            page.imageHeight = image.height
            page.imageWidth = image.width
            page.index = index
            if (newPage && index == 0) {
                page.type = "Front Cover"
            }
            if (newPage && index == imageList.size - 1) {
                page.type = "Back Cover"
            }
            val newFilename = it.parent / (filename + "_${index.toString().padStart(length = padCount, padChar = '0')}." + it.extension)
            if (it.name != newFilename.name) {
                Console.print("Renamed ${it.name} to ${newFilename.name}")
                logger.info("Renamed ${it.name} to ${newFilename.name}")
                it.moveTo(newFilename, overwrite = false)
            }
            page.filename = newFilename.name

            val pages = metadata.pages.toMutableList()
            if (pages.size > index) {
                pages[index] = page
            } else {
                pages.add(page)
            }
            metadata.pages = pages.toList()
        }
    }

    private fun removeEmptyDirectories(directory: File) {
        directory.listFiles()?.forEach {
            if (it.isDirectory) {
                removeEmptyDirectories(directory = it)
            }
        }
        if (!directory.name.startsWith(".") && (directory.listFiles()?.size ?: 0) == 0) {
            logger.info("Cleaning up blank folder: ${directory.name}")
            directory.deleteRecursively()
        }
    }

    fun start(settings: Settings) {
        convertCollection(directory = settings.collectionFolder)
        var metron: MetronTalker? = null
        if (!settings.metron.username.isNullOrBlank() && !settings.metron.password.isNullOrBlank()) {
            metron = MetronTalker(settings = settings.metron)
        }
        var comicvine: ComicvineTalker? = null
        if (!settings.comicvine.apiKey.isNullOrBlank()) {
            comicvine = ComicvineTalker(settings = settings.comicvine)
        }
        readCollection(directory = settings.collectionFolder).forEach { (file, _metadata) ->
            Console.print("Processing ${file.nameWithoutExtension}")
            val metadata = _metadata ?: Metadata(
                issue = Metadata.Issue(
                    publisher = Metadata.Issue.Publisher(
                        title = Console.prompt(prompt = "Publisher title") ?: return@forEach,
                    ),
                    series = Metadata.Issue.Series(
                        title = Console.prompt(prompt = "Series title") ?: return@forEach,
                    ),
                    number = Console.prompt(prompt = "Issue number") ?: return@forEach,
                ),
            )
            Console.print("Using Metron to look for information")
            var success = metron?.pullMetadata(metadata = metadata) ?: false
            if (!success) {
                logger.warn("Unable to pull info from Metron")
                Console.print("Using Comicvine to look for information")
                success = comicvine?.pullMetadata(metadata = metadata) ?: false
            }
            if (!success) {
                logger.warn("Unable to pull info from Comicvine")
            }
            val tempDir = createTempDirectory(prefix = "${file.nameWithoutExtension}_")
            ZipUtils.unzip(srcFile = file, destFolder = tempDir)
            val tempFile = file.parent / (file.name + ".temp")
            file.moveTo(target = tempFile)
            val filename = metadata.issue.getFilename()
            parsePages(folder = tempDir, metadata = metadata, filename = filename)

            metadata.toFile(tempDir / "Metadata.xml")
            metadata.toMetronInfo()?.toFile(tempDir / "MetronInfo.xml")
            metadata.toComicInfo().toFile(tempDir / "ComicInfo.xml")

            ZipUtils.zip(destFile = file.parent / "$filename.cbz", content = Utils.listFiles(path = tempDir))
            tempDir.toFile().deleteRecursively()
            tempFile.toFile().delete()
        }
        readCollection(directory = settings.collectionFolder)
            .filterValues { it != null }
            .mapValues { it.value as Metadata }
            .forEach { (file, metadata) ->
                val newLocation = Paths.get(
                    settings.collectionFolder.pathString,
                    metadata.issue.publisher.getFilename(),
                    metadata.issue.series.getFilename(),
                    "${metadata.issue.getFilename()}.${file.extension}",
                )
                if (file != newLocation) {
                    Console.print(
                        "Renamed ${file.relativeTo(settings.collectionFolder)} to ${newLocation.relativeTo(settings.collectionFolder)}",
                    )
                    logger.info(
                        "Renamed ${file.relativeTo(settings.collectionFolder)} to ${newLocation.relativeTo(settings.collectionFolder)}",
                    )
                    newLocation.parent.toFile().mkdirs()
                    file.moveTo(newLocation, overwrite = false)
                }
            }
        removeEmptyDirectories(directory = settings.collectionFolder.toFile())
    }
}

fun main(
    @Suppress("UNUSED_PARAMETER") vararg args: String,
) {
    println("Kilowog v${Utils.VERSION}")
    println("Kotlin v${KotlinVersion.CURRENT}")
    println("Java v${System.getProperty("java.version")}")
    println("Arch: ${System.getProperty("os.arch")}")
    val settings = Settings.load()
    println(settings.toString())
    App.start(settings = settings)
}
