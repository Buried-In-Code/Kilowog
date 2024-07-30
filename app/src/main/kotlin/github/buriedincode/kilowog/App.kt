package github.buriedincode.kilowog

import github.buriedincode.kilowog.Details.Identifications
import github.buriedincode.kilowog.Settings.Output
import github.buriedincode.kilowog.Utils.titlecase
import github.buriedincode.kilowog.archives.BaseArchive
import github.buriedincode.kilowog.archives.CB7Archive
import github.buriedincode.kilowog.archives.CBTArchive
import github.buriedincode.kilowog.archives.CBZArchive
import github.buriedincode.kilowog.archives.getArchive
import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.ComicInfo
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.MetronInfo
import github.buriedincode.kilowog.models.getMetadataId
import github.buriedincode.kilowog.models.metadata.Format
import github.buriedincode.kilowog.models.metadata.Meta
import github.buriedincode.kilowog.models.metadata.Source
import github.buriedincode.kilowog.models.metadata.Tool
import github.buriedincode.kilowog.models.metroninfo.InformationSource
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toKotlinLocalDate
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.div
import kotlin.io.path.name

object App {
    @JvmStatic
    private val LOGGER = KotlinLogging.logger { }

    /*var comicvine: ComicvineTalker? = null
    var metron: MetronTalker? = null

    private fun parsePages(folder: Path, metadata: Metadata, filename: String) {
        val imageList = Utils.listFiles(path = folder, extensions = Utils.imageExtensions).sorted()
        val padLength = imageList.size.toString().length
        imageList.forEachIndexed { index, it ->
            val page = if (metadata.pages.getOrNull(index = index) == null) {
                val image = ImageIO.read(it.toFile())
                Page(
                    doublePage = image.width >= image.height,
                    filename = it.name,
                    size = it.fileSize(),
                    height = image.height,
                    width = image.width,
                    index = index,
                    type = when (index) {
                        0 -> PageType.FRONT_COVER
                        imageList.size - 1 -> PageType.BACK_COVER
                        else -> PageType.STORY
                    },
                )
            } else {
                metadata.pages.get(index = index)
            }

            val newFilename = it.parent / (filename + "_${index.toString().padStart(length = padLength, padChar = '0')}." + it.extension)
            if (it.name != newFilename.name) {
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

    private fun fetchFromServices(settings: Settings, metaInfo: MetaInfo) {
        var marvel: MarvelTalker? = null
        if (settings.marvel != null && !settings.marvel.publicKey.isNullOrBlank() && !settings.marvel.privateKey.isNullOrBlank()) {
            marvel = MarvelTalker(settings = settings.marvel)
        }
        var metron: MetronTalker? = null
        if (settings.metron != null && !settings.metron.username.isNullOrBlank() && !settings.metron.password.isNullOrBlank()) {
            metron = MetronTalker(settings = settings.metron)
        }
        var comicvine: ComicvineTalker? = null
        if (settings.comicvine != null && !settings.comicvine.apiKey.isNullOrBlank()) {
            comicvine = ComicvineTalker(settings = settings.comicvine)
        }
        var league: LeagueTalker? = null
        if (
            settings.leagueOfComicGeeks != null &&
            !settings.leagueOfComicGeeks.clientId.isNullOrBlank() &&
            !settings.leagueOfComicGeeks.clientSecret.isNullOrBlank()
        ) {
            league = LeagueTalker(settings = settings.leagueOfComicGeeks)
        }
        if (metron == null && comicvine == null) {
            logger.warn("No external services configured")
            return
        }

        var success = marvel?.fetch(metadata = metaInfo.metadata, metronInfo = metaInfo.metronInfo, comicInfo = metaInfo.comicInfo) ?: false
        if (!success) {
            success = metron?.fetch(metadata = metaInfo.metadata, metronInfo = metaInfo.metronInfo, comicInfo = metaInfo.comicInfo) ?: false
        }
        if (!success) {
            success = comicvine?.fetch(
                metadata = metaInfo.metadata,
                metronInfo = metaInfo.metronInfo,
                comicInfo = metaInfo.comicInfo,
            ) ?: false
        }
        if (!success) {
            success = league?.fetch(metadata = metaInfo.metadata, metronInfo = metaInfo.metronInfo, comicInfo = metaInfo.comicInfo) ?: false
        }
        if (!success) {
            logger.warn("Unable to fetch from services")
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun removeEmptyDirectories(directory: Path) {
        Utils.listFiles(path = directory).forEach {
            if (it.isDirectory()) {
                removeEmptyDirectories(directory = it)
            }
        }
        if (directory.name.startsWith(".") && Utils.listFiles(path = directory).isEmpty()) {
            logger.info("Removing blank folder: ${directory.name}")
            directory.deleteRecursively()
        }
    }*/

    private fun convertCollection(path: Path, output: Output.Format) {
        val (format, archiveType) = when (output) {
            Output.Format.CB7 -> Pair("cb7", CB7Archive)
            Output.Format.CBT -> Pair("cbt", CBTArchive)
            else -> Pair("cbz", CBZArchive)
        }
        val formats = Utils.archiveExtensions.filter { it != format }
        Utils.listFiles(path = path, extensions = formats.toTypedArray()).map {
            val message = "Converting ${it.name} to ${output.titlecase()}"
            print("\r[      ] $message")
            val archive = getArchive(path = it)
            archiveType.convert(oldArchive = archive)
            println("\r[ DONE ] $message")
        }
    }

    private fun readMeta(archive: BaseArchive): Pair<Meta?, Details?> {
        Metadata.fromArchive(archive = archive)?.let {
            val meta = it.meta
            val details = Details(
                series = Identifications(
                    search = it.issue.series.title,
                    comicvine = getMetadataId(it.issue.series.resources, Source.COMICVINE),
                    league = getMetadataId(it.issue.series.resources, Source.LEAGUE_OF_COMIC_GEEKS),
                    marvel = getMetadataId(it.issue.series.resources, Source.MARVEL),
                    metron = getMetadataId(it.issue.series.resources, Source.METRON),
                ),
                issue = Identifications(
                    search = it.issue.number,
                    comicvine = getMetadataId(it.issue.resources, Source.COMICVINE),
                    league = getMetadataId(it.issue.resources, Source.LEAGUE_OF_COMIC_GEEKS),
                    marvel = getMetadataId(it.issue.resources, Source.MARVEL),
                    metron = getMetadataId(it.issue.resources, Source.METRON),
                ),
            )
            return Pair(meta, details)
        }
        MetronInfo.fromArchive(archive = archive)?.let {
            val seriesId = if (it.id == null) null else it.series.id
            val issueId = it.id?.primary?.value
            val source = it.id?.primary?.source
            val details = Details(
                series = Identifications(
                    search = it.series.name,
                    comicvine = if (source == InformationSource.COMIC_VINE) seriesId else null,
                    league = if (source == InformationSource.LEAGUE_OF_COMIC_GEEKS) seriesId else null,
                    marvel = if (source == InformationSource.MARVEL) seriesId else null,
                    metron = if (source == InformationSource.MARVEL) seriesId else null,
                ),
                issue = Identifications(
                    search = it.number,
                    comicvine = if (source == InformationSource.COMIC_VINE) issueId else null,
                    league = if (source == InformationSource.LEAGUE_OF_COMIC_GEEKS) issueId else null,
                    marvel = if (source == InformationSource.MARVEL) issueId else null,
                    metron = if (source == InformationSource.MARVEL) issueId else null,
                ),
            )
            return Pair(Meta(LocalDate.now().toKotlinLocalDate(), Tool("MetronInfo")), details)
        }
        ComicInfo.fromArchive(archive = archive)?.let {
            val details = Details(series = Identifications(search = it.series), issue = Identifications(search = it.number))
            return Pair(Meta(LocalDate.now().toKotlinLocalDate(), Tool("ComicInfo")), details)
        }
        return Pair(null, null)
    }

    private fun loadArchives(path: Path, output: Output.Format, force: Boolean = false): List<Triple<Path, BaseArchive, Details?>> {
        val archives = mutableListOf<Triple<Path, BaseArchive, Details?>>()
        Utils.listFiles(path = path, output.name.lowercase()).forEach {
            val archive = getArchive(path = it)
            LOGGER.debug { "Reading ${it.name}" }
            val (meta, details) = readMeta(archive = archive)
            if (meta == null ||
                details == null ||
                force ||
                meta.tool != Tool() ||
                meta.date.daysUntil(LocalDate.now().toKotlinLocalDate()) >= 28
            ) {
                archives.add(Triple(it, archive, details))
            }
        }
        return archives
    }

    fun start(settings: Settings, force: Boolean = false) {
        LOGGER.info { "Starting Kilowog" }
        convertCollection(path = settings.inputFolder, output = settings.output.format)

        val message = "Searching for ${settings.output.format} files"
        print("\r[      ] $message")
        val archives = loadArchives(path = settings.inputFolder, output = settings.output.format, force = force)
        println("\r[ DONE ] $message")

        archives.forEach { (file, archive, _details) ->
            LOGGER.info { "Processing ${file.name}" }
            val details = _details ?: Details(
                series = Identifications(search = Console.prompt("Series title")),
                issue = Identifications(),
            )
            LOGGER.warn { "File: $file" }
            LOGGER.warn { "Archive: $archive" }
            LOGGER.warn { "Details: $details" }
        }

        /*Utils
            .listFiles(
                settings.collectionFolder,
                settings.output.format.name
                    .lowercase(),
            ).forEach {
                val archive = getArchive(path = it)
                val metaInfo = readMetaInfo(archive = archive, settings = settings.output)

                if (!force && metaInfo.metadata != null) {
                    val now = LocalDate.now()
                    if (metaInfo.metadata.meta.tool == Tool() &&
                        metaInfo.metadata.meta.date
                            .toJavaLocalDate()
                            .isAfter(now.minusDays(28))
                    ) {
                        return@forEach
                    }
                }

                logger.info("Processing ${it.name}")
                fetchFromServices(settings = settings, metaInfo = metaInfo)
            }

        Utils
            .listFiles(
                path = settings.collectionFolder,
                settings.output.format.name
                    .lowercase(),
            ).forEach {
                val archive = getArchive(path = it)
                val (metadata, metronInfo, comicInfo) = readMetaInfo(archive = archive, settings = settings.output)
                val publisherFilename = metadata
                    ?.issue
                    ?.series
                    ?.publisher
                    ?.title
                    ?: metronInfo?.publisher?.value
                    ?: comicInfo?.publisher
                    ?: return@forEach
                val seriesTitle = metadata?.issue?.series?.title
                    ?: metronInfo?.series?.name
                    ?: comicInfo?.series
                    ?: return@forEach
                val seriesVolume = metadata?.issue?.series?.volume
                    ?: metronInfo?.series?.volume
                    ?: comicInfo?.volume
                    ?: return@forEach
                val seriesFilename = if (seriesVolume == 1) seriesTitle else "$seriesTitle v$seriesVolume"
                val issueFilename = metadata?.let {
                    val numberStr = it.issue.number?.let { number ->
                        "_#${number.padStart(if (it.issue.format == Format.COMIC) 3 else 2, '0')}"
                    } ?: ""
                    val formatStr = when (it.issue.format) {
                        Format.ANNUAL -> "_Annual"
                        Format.DIGITAL_CHAPTER -> "_Chapter"
                        Format.GRAPHIC_NOVEL -> "_GN"
                        Format.HARDCOVER -> "_HC"
                        Format.TRADE_PAPERBACK -> "_TP"
                        else -> ""
                    }
                    when (it.issue.format) {
                        Format.ANNUAL, Format.DIGITAL_CHAPTER -> seriesFilename + formatStr + numberStr
                        Format.GRAPHIC_NOVEL, Format.HARDCOVER, Format.TRADE_PAPERBACK -> seriesFilename + numberStr + formatStr
                        else -> seriesFilename + numberStr
                    }
                } ?: metronInfo?.let {
                    val numberStr = it.number?.let { number ->
                        "_#${number.padStart(if (it.series.format == MetronFormat.SERIES) 3 else 2, '0')}"
                    } ?: ""
                    val formatStr = when (it.series.format) {
                        MetronFormat.ANNUAL -> "_Annual"
                        MetronFormat.GRAPHIC_NOVEL -> "_GN"
                        MetronFormat.TRADE_PAPERBACK -> "_TP"
                        else -> ""
                    }
                    when (it.series.format) {
                        MetronFormat.ANNUAL -> seriesFilename + formatStr + numberStr
                        MetronFormat.GRAPHIC_NOVEL, MetronFormat.TRADE_PAPERBACK -> seriesFilename + numberStr + formatStr
                        else -> seriesFilename + numberStr
                    }
                }
                    ?: comicInfo?.let {
                        seriesFilename + it.number
                    }
                    ?: return@forEach
                val newLocation = settings.collectionFolder / Utils.sanitize(
                    value = publisherFilename,
                ) / Utils.sanitize(value = seriesFilename) / Utils.sanitize(value = issueFilename)
                if (it == newLocation) {
                    logger.info("Moved ${it.relativeTo(settings.collectionFolder)} to ${newLocation.relativeTo(settings.collectionFolder)}")
                    newLocation.parent.toFile().mkdirs()
                    it.moveTo(newLocation, overwrite = false)
                }
            }
        removeEmptyDirectories(directory = settings.collectionFolder)*/
    }
}

fun main(vararg args: String) {
    println("Kilowog v${Utils.VERSION}")
    println("Kotlin v${KotlinVersion.CURRENT}")
    println("Java v${System.getProperty("java.version")}")
    println("Arch: ${System.getProperty("os.arch")}")

    println("Args: ${args.contentToString()}")

    val settings = Settings.load()
    println(settings.toString())

    App.start(settings = settings, force = args.firstOrNull().equals("force", ignoreCase = true))
}
