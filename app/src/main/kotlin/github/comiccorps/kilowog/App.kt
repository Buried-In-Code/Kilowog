package github.comiccorps.kilowog

import github.comiccorps.kilowog.Utils.isNullOrBlank
import github.comiccorps.kilowog.archives.BaseArchive
import github.comiccorps.kilowog.archives.CB7Archive
import github.comiccorps.kilowog.archives.CBRArchive
import github.comiccorps.kilowog.archives.CBTArchive
import github.comiccorps.kilowog.archives.CBZArchive
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.MetaInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo
import github.comiccorps.kilowog.models.metadata.Format
import github.comiccorps.kilowog.models.metadata.Page
import github.comiccorps.kilowog.models.metadata.PageType
import github.comiccorps.kilowog.models.metadata.Tool
import github.comiccorps.kilowog.services.ComicvineTalker
import github.comiccorps.kilowog.services.MetronTalker
import kotlinx.datetime.toJavaLocalDate
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Path
import java.time.LocalDate
import javax.imageio.ImageIO
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.relativeTo
import github.comiccorps.kilowog.models.metroninfo.Format as MetronFormat

object App : Logging {
    var comicvine: ComicvineTalker? = null
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

    private fun getArchive(path: Path): BaseArchive {
        return when (path.extension) {
            "cb7" -> CB7Archive(path = path)
            "cbr" -> CBRArchive(path = path)
            "cbt" -> CBTArchive(path = path)
            "cbz" -> CBZArchive(path = path)
            else -> throw NotImplementedError("${path.name} is an unsupported archive")
        }
    }

    private fun convertCollection(path: Path, output: Settings.Output.Format) {
        val formats = mutableListOf(".cbr")
        when (output) {
            Settings.Output.Format.CB7 -> {
                formats.addAll(arrayOf(".cbt", ".cbz"))
            }
            Settings.Output.Format.CBT -> {
                formats.addAll(arrayOf(".cb7", ".cbz"))
            }
            else -> {
                formats.addAll(arrayOf(".cb7", ".cbt"))
            }
        }
        Utils.listFiles(path = path, extensions = formats.toTypedArray()).forEach {
            logger.info("Converting ${it.name} to ${output.name.lowercase()} format")
            val archive = getArchive(path = it)
            when (output) {
                Settings.Output.Format.CB7 -> CB7Archive.convert(old = archive)
                Settings.Output.Format.CBT -> CBTArchive.convert(old = archive)
                Settings.Output.Format.CBZ -> CBZArchive.convert(old = archive)
            }
        }
    }

    private fun readMetaInfo(archive: BaseArchive, settings: Settings.Output): MetaInfo {
        val filenames = archive.listFilenames()
        var metadata: Metadata? = if ("/Metadata.xml" in filenames || "MetronInfo.xml" in filenames) {
            Metadata.fromArchive(archive = archive)
        } else {
            null
        }
        var metronInfo: MetronInfo? = if ("/MetronInfo.xml" in filenames || "MetronInfo.xml" in filenames) {
            MetronInfo.fromArchive(archive = archive)
        } else {
            null
        }
        var comicInfo: ComicInfo? = if ("/ComicInfo.xml" in filenames || "ComicInfo.xml" in filenames) {
            ComicInfo.fromArchive(archive = archive)
        } else {
            null
        }

        if (metadata == null) {
            metadata = metronInfo?.toMetadata()
                ?: comicInfo?.toMetadata()
                ?: Metadata.create(archive = archive)
        }
        if (metronInfo == null) {
            metronInfo = metadata.toMetronInfo()
        }
        if (comicInfo == null) {
            comicInfo = metadata.toComicInfo()
        }

        return MetaInfo(
            metadata = if (settings.createMetadata) metadata else null,
            metronInfo = if (settings.createMetronInfo) metronInfo else null,
            comicInfo = if (settings.createComicInfo) comicInfo else null,
        )
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
    }

    fun start(settings: Settings, force: Boolean = false) {
        logger.info("Starting Kilowog")
        convertCollection(path = settings.collectionFolder, output = settings.output.format)
        Utils
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
        removeEmptyDirectories(directory = settings.collectionFolder)
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
