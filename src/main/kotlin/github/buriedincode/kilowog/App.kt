package github.buriedincode.kilowog

import com.github.junrar.Archive
import com.github.junrar.Junrar
import com.github.junrar.rarfile.FileHeader
import github.buriedincode.kilowog.Utils.isNullOrBlank
import github.buriedincode.kilowog.comicinfo.ComicInfo
import github.buriedincode.kilowog.comicinfo.enums.Manga
import github.buriedincode.kilowog.comicinfo.enums.YesNo
import github.buriedincode.kilowog.metadata.Metadata
import github.buriedincode.kilowog.metadata.enums.Source
import github.buriedincode.kilowog.metroninfo.MetronInfo
import github.buriedincode.kilowog.metroninfo.enums.Format
import github.buriedincode.kilowog.metroninfo.enums.Genre
import github.buriedincode.kilowog.metroninfo.enums.InformationSource
import github.buriedincode.kilowog.metroninfo.enums.Role
import github.buriedincode.kilowog.services.Comicvine
import github.buriedincode.kilowog.services.Metron
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.kotlin.Logging
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.extension
import kotlin.io.path.moveTo
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

object App : Logging {
    private fun testComicInfo() {
        val comicInfo = ComicInfo(
            blackAndWhite = YesNo.NO,
            day = 7,
            format = "Comic",
            language = "en",
            manga = Manga.NO,
            month = 4,
            notes = "Scraped metadata from Comixology [CMXDB929703], [ASINB08ZKFQBRJ]",
            number = "1",
            pageCount = 32,
            publisher = "BOOM! Studios",
            series = "Magic: The Gathering",
            summary = "* A new beginning for the pop culture phenomenon of Magic starts here from Jed MacKay (Marvel's Black Cat)...",
            title = "Magic: The Gathering #1",
            volume = 1,
            year = 2021,
        ).apply {
            characterList = listOf("Jace Beleren", "Kaya", "Lavinia", "Ral Zarek", "Vraska")
            colouristList = listOf("Arianna Consonni")
            coverArtistList = listOf("Matteo Scalera", "Moreno DiNisio")
            editorList = listOf("Amanda Lafranco", "Bryce Carlson")
            genreList = listOf("Fantasy", "Other")
            lettererList = listOf("Ed Dukeshire")
            locationList = listOf("Ravnica", "Zendikar")
            teamList = listOf("Planeswalkers")
            writerList = listOf("Jed MacKay")
        }
        logger.warn(comicInfo)
        val comicInfoStr = Utils.XML_MAPPER.encodeToString(comicInfo)
        logger.warn(comicInfoStr)
    }
    private fun testMetronInfo() {
        val metronInfo = MetronInfo(
            blackAndWhite = false,
            characters = listOf(
                MetronInfo.Resource(id = -1, value = "Jace Beleren"),
                MetronInfo.Resource(id = -1, value = "Kaya"),
                MetronInfo.Resource(id = -1, value = "Lavinia"),
                MetronInfo.Resource(id = -1, value = "Ral Zarek"),
                MetronInfo.Resource(id = -1, value = "Vraska"),
            ),
            coverDate = LocalDate(year = 2021, monthNumber = 4, dayOfMonth = 7),
            credits = listOf(
                MetronInfo.Credit(
                    creator = MetronInfo.Resource(id = -1, value = "Aaron Bartling"),
                    roles = listOf(
                        MetronInfo.RoleResource(id = -1, value = Role.COVER),
                    ),
                ),
                MetronInfo.Credit(
                    creator = MetronInfo.Resource(id = -1, value = "Alan Quah"),
                    roles = listOf(
                        MetronInfo.RoleResource(id = -1, value = Role.COVER),
                    ),
                ),
            ),
            genres = listOf(
                MetronInfo.GenreResource(id = -1, value = Genre.FANTASY),
            ),
            id = MetronInfo.Source(source = InformationSource.LEAGUE_OF_COMIC_GEEKS, value = 3694173),
            locations = listOf(
                MetronInfo.Resource(id = -1, value = "Ravnica"),
                MetronInfo.Resource(id = -1, value = "Zendikar"),
            ),
            notes = "Scraped metadata from Comixology [CMXDB929703], [ASINB08ZKFQBRJ]",
            number = "1",
            pageCount = 32,
            publisher = MetronInfo.Resource(id = 13, value = "BOOM! Studios"),
            series = MetronInfo.Series(
                format = Format.SERIES,
                lang = "en",
                id = 150717,
                name = "Magic: The Gathering",
                sortName = "Magic: The Gathering",
                volume = 1,
            ),
            storeDate = LocalDate(year = 2021, monthNumber = 4, dayOfMonth = 7),
            summary = "* A new begging for the pop culture...",
            title = "Magic: The Gathering #1",
            teams = listOf(
                MetronInfo.Resource(id = -1, value = "Planeswalkers"),
            ),
        )
        logger.warn(metronInfo)
        val metronInfoStr = Utils.XML_MAPPER.encodeToString(metronInfo)
        logger.warn(metronInfoStr)
    }
    private fun testMetadata() {
        val metadata = Metadata(
            issue = Metadata.Issue(
                publisher = Metadata.Issue.Publisher(
                    resources = listOf(
                        Metadata.Issue.Resource(
                            source = Source.COMICVINE,
                            value = 1868,
                        ),
                        Metadata.Issue.Resource(
                            source = Source.LEAGUE_OF_COMIC_GEEKS,
                            value = 13,
                        ),
                        Metadata.Issue.Resource(
                            source = Source.METRON,
                            value = 20,
                        ),
                    ),
                    title = "BOOM! Studios",
                ),
                series = Metadata.Issue.Series(
                    format = "Comic",
                    resources = listOf(
                        Metadata.Issue.Resource(
                            source = Source.COMICVINE,
                            value = 135280,
                        ),
                        Metadata.Issue.Resource(
                            source = Source.LEAGUE_OF_COMIC_GEEKS,
                            value = 150717,
                        ),
                    ),
                    startYear = 2021,
                    title = "Magic: The Gathering",
                    volume = 1,
                ),
                number = "1",
                title = "Magic: The Gathering #1",
                characters = listOf(
                    Metadata.Issue.NamedResource(name = "Jace Beleren"),
                    Metadata.Issue.NamedResource(name = "Kaya"),
                    Metadata.Issue.NamedResource(name = "Lavinia"),
                    Metadata.Issue.NamedResource(name = "Ral Zarek"),
                    Metadata.Issue.NamedResource(name = "Vraska"),
                ),
                coverDate = LocalDate(year = 2021, monthNumber = 4, dayOfMonth = 7),
                credits = listOf(
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = "Aaron Bartling"),
                        roles = listOf("Variant Cover Artist"),
                    ),
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = "Ig Guara"),
                        roles = listOf("Artist", "Variant Cover Artist"),
                    ),
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = "Scott Newman"),
                        roles = listOf("Designer"),
                    ),
                ),
                genres = listOf("Fantasy", "Other"),
                language = "en",
                locations = listOf(
                    Metadata.Issue.NamedResource(name = "Ravnica"),
                    Metadata.Issue.NamedResource(name = "Zendikar"),
                ),
                pageCount = 32,
                resources = listOf(
                    Metadata.Issue.Resource(
                        source = Source.COMICVINE,
                        value = 842154,
                    ),
                    Metadata.Issue.Resource(
                        source = Source.LEAGUE_OF_COMIC_GEEKS,
                        value = 3694173,
                    ),
                ),
                storeDate = LocalDate(year = 2021, monthNumber = 4, dayOfMonth = 7),
                summary = "* A new beginning for the pop culture",
                teams = listOf(
                    Metadata.Issue.NamedResource(name = "Planeswalkers"),
                ),
            ),
            notes = "Scraped metadata from Comixology [CMXDB929703], [ASINB08ZKFQBRJ]",
        )
        logger.warn(metadata)
        val metadataStr = Utils.XML_MAPPER.encodeToString(metadata)
        logger.warn(metadataStr)
    }

    private fun findInfoFile(archive: Archive, filename: String): FileHeader? {
        for (fileHeader in archive.fileHeaders) {
            if (fileHeader.fileName.equals(filename, ignoreCase = true)) {
                return fileHeader
            }
        }
        return null
    }

    private fun readInfoFile(archiveFile: File, infoFile: String): String? {
        val tempFile = createTempFile(prefix = "${archiveFile.name}__${infoFile}__", suffix = ".xml").toFile()
        tempFile.deleteOnExit()

        if (archiveFile.extension == "cbz") {
            val zip = ZipFile(archiveFile)
            val entry = zip.getEntry("$infoFile.xml") ?: return null
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
        } catch (exc: MissingFieldException) {
            // logger.fatal(content)
            logger.fatal(exc)
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetronInfo(archiveFile: File): MetronInfo? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "MetronInfo")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<MetronInfo>(content)
        } catch (exc: MissingFieldException) {
            // logger.fatal(content)
            logger.fatal(exc)
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readComicInfo(archiveFile: File): ComicInfo? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "ComicInfo")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<ComicInfo>(content)
        } catch (exc: MissingFieldException) {
            // logger.fatal(content)
            logger.fatal(exc)
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

    fun convertCbrToCbz(directory: Path) {
        Utils.listFiles(directory, "cbr").forEach { srcFile ->
            val tempDir = createTempDirectory(srcFile.pathString)
            Utils.recursiveDeleteOnExit(path = tempDir)

            Junrar.extract(srcFile.toFile(), tempDir.toFile())

            val destFile = Path(srcFile.parent.pathString, srcFile.nameWithoutExtension + ".cbz")
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destFile.toFile()))).use { out ->
                Utils.listFiles(tempDir).map { it.pathString }.forEach { file ->
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
    }

    fun start(settings: Settings) {
        val comicvine: Comicvine? = if (settings.comicvine.apiKey.isNullOrBlank()) null else Comicvine(apiKey = settings.comicvine.apiKey!!)
        val metron: Metron? = if (settings.metron.username.isNullOrBlank() || settings.metron.password.isNullOrBlank()) {
            null
        } else {
            Metron(
                username = settings.metron.username,
                password = settings.metron.password!!,
            )
        }
        // testComicInfo()
        // testMetronInfo()
        // testMetadata()
        convertCbrToCbz(directory = settings.collectionFolder)
        val collection = readCollection(directory = settings.collectionFolder)
        collection.filterValues { it != null }.mapValues { it.value as Metadata }.firstNotNullOf { (file, metadata) ->
            logger.debug(metadata)
            if (metadata.issue.publisher.resources.isNotEmpty()) {
                logger.info("Publisher Id exists")
            } else {
                val publishers = comicvine?.listPublishers(name = metadata.issue.publisher.imprint ?: metadata.issue.publisher.title)
                println(publishers)
            }
        }
        collection.filterValues { it != null }.mapValues { it.value as Metadata }.forEach { (file, metadata) ->
            val newLocation = Paths.get(
                settings.collectionFolder.pathString,
                metadata.issue.publisher.getFilename(),
                metadata.issue.series.getFilename(),
                "${metadata.issue.getFilename()}.${file.extension}",
            )
            if (file != newLocation) {
                logger.info("Renamed $file to $newLocation")
                newLocation.parent.toFile().mkdirs()
                file.moveTo(newLocation, overwrite = false)
            }
        }
        Utils.removeBlankDirectories(directory = settings.collectionFolder.toFile())
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
