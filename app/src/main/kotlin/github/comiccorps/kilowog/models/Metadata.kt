package github.comiccorps.kilowog.models

import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.Utils.asEnumOrNull
import github.comiccorps.kilowog.Utils.titlecase
import github.comiccorps.kilowog.archives.BaseArchive
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.metadata.Issue
import github.comiccorps.kilowog.models.metadata.Meta
import github.comiccorps.kilowog.models.metadata.Page
import github.comiccorps.kilowog.models.metadata.Series
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metadata.TitledResource
import github.comiccorps.kilowog.models.metadata.Tool
import github.comiccorps.kilowog.models.metroninfo.Arc
import github.comiccorps.kilowog.models.metroninfo.Format
import github.comiccorps.kilowog.models.metroninfo.Genre
import github.comiccorps.kilowog.models.metroninfo.GenreResource
import github.comiccorps.kilowog.models.metroninfo.InformationSource
import github.comiccorps.kilowog.models.metroninfo.Role
import github.comiccorps.kilowog.models.metroninfo.RoleResource
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.name
import kotlin.io.path.writeText
import github.comiccorps.kilowog.models.comicinfo.Page as ComicPage
import github.comiccorps.kilowog.models.comicinfo.PageType as ComicPageType
import github.comiccorps.kilowog.models.metroninfo.Credit as MetronCredit
import github.comiccorps.kilowog.models.metroninfo.Page as MetronPage
import github.comiccorps.kilowog.models.metroninfo.PageType as MetronPageType
import github.comiccorps.kilowog.models.metroninfo.Resource as MetronResource
import github.comiccorps.kilowog.models.metroninfo.Series as MetronSeries
import github.comiccorps.kilowog.models.metroninfo.Source as MetronSource

@Serializable
class Metadata(
    @XmlSerialName("Issue")
    var issue: Issue,
    @XmlSerialName("Meta")
    var meta: Meta,
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
) : Comparable<Metadata> {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    var schemaUrl: String = Metadata.schemaUrl

    fun toComicInfo(): ComicInfo {
        return ComicInfo(
            format = this.issue.format.titlecase(),
            language = this.issue.language,
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                ComicPage(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.height,
                    imageSize = it.size,
                    imageWidth = it.width,
                    type = it.type.name.asEnumOrNull<ComicPageType>() ?: ComicPageType.STORY,
                )
            },
            publisher = this.issue.series.publisher.title,
            series = this.issue.series.title,
            summary = this.issue.summary,
            title = this.issue.title,
            volume = this.issue.series.volume,
        ).apply {
            this.characterList = this@Metadata.issue.characters.map { it.title }
            this.credits = this@Metadata.issue.credits.associate {
                it.creator.title to it.roles.map { it.title }
            }
            this.coverDate = this@Metadata.issue.coverDate
            this.genreList = this@Metadata
                .issue.series.genres
                .map { it.title }
            this.locationList = this@Metadata.issue.locations.map { it.title }
            this.storyArcList = this@Metadata.issue.storyArcs.map { it.title }
            this.teamList = this@Metadata.issue.teams.map { it.title }
        }
    }

    fun toMetronInfo(): MetronInfo? {
        val source = this.issue.resources
            .firstOrNull { it.source == Source.METRON }
            ?.source
            ?: this.issue.resources
                .firstOrNull { it.source == Source.COMICVINE }
                ?.source
        return MetronInfo(
            arcs = this.issue.storyArcs.map {
                Arc(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    name = it.title,
                    number = it.number,
                )
            },
            characters = this.issue.characters.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.title,
                )
            },
            coverDate = this.issue.coverDate ?: return null,
            credits = this.issue.credits.map {
                MetronCredit(
                    creator = MetronResource(
                        id = it.creator.resources
                            .firstOrNull { it.source == source }
                            ?.value,
                        value = it.creator.title,
                    ),
                    roles = it.roles.mapNotNull {
                        RoleResource(
                            id = it.resources.firstOrNull { it.source == source }?.value,
                            value = it.title.asEnumOrNull<Role>() ?: return@mapNotNull null,
                        )
                    },
                )
            },
            genres = this.issue.series.genres.mapNotNull {
                GenreResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.title.asEnumOrNull<Genre>() ?: return@mapNotNull null,
                )
            },
            id = source?.titlecase()?.asEnumOrNull<InformationSource>()?.let {
                MetronSource(
                    source = it,
                    value = this.issue.resources
                        .firstOrNull { it.source == source }
                        ?.value ?: return@let null,
                )
            },
            locations = this.issue.locations.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.title,
                )
            },
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                MetronPage(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.height,
                    imageSize = it.size,
                    imageWidth = it.width,
                    type = it.type.name.asEnumOrNull<MetronPageType>() ?: MetronPageType.STORY,
                )
            },
            publisher = MetronResource(
                id = this.issue.series.publisher.resources
                    .firstOrNull { it.source == source }
                    ?.value,
                value = this.issue.series.publisher.title,
            ),
            series = MetronSeries(
                format = this.issue.format
                    .titlecase()
                    .asEnumOrNull<Format>() ?: Format.SERIES,
                id = this.issue.series.resources
                    .firstOrNull { it.source == source }
                    ?.value,
                lang = this.issue.language,
                name = this.issue.series.title,
                volume = this.issue.series.volume,
            ),
            storeDate = this.issue.storeDate,
            summary = this.issue.summary,
            teams = this.issue.teams.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.title,
                )
            },
            title = this.issue.title,
        )
    }

    fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun compareTo(other: Metadata): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        return issue == other.issue
    }

    override fun hashCode(): Int {
        return issue.hashCode()
    }

    override fun toString(): String {
        return "Metadata(issue=$issue, meta=$meta, notes=$notes, pages=$pages)"
    }

    companion object : Logging {
        val schemaUrl: String = "https://raw.githubusercontent.com/ComicCorps/Schemas/main/drafts/v1.0/Metadata.xsd"

        private val comparator = compareBy(Metadata::issue)

        @OptIn(ExperimentalSerializationApi::class)
        fun fromArchive(archive: BaseArchive): Metadata? {
            return try {
                archive.readFile(filename = "/Metadata.xml")?.let { Utils.XML_MAPPER.decodeFromString<Metadata>(it) }
                    ?: archive.readFile(filename = "Metadata.xml")?.let { Utils.XML_MAPPER.decodeFromString<Metadata>(it) }
            } catch (mfe: MissingFieldException) {
                logger.error("${archive.path.name} contains an invalid Metadata file: ${mfe.message}")
                null
            } catch (se: SerializationException) {
                logger.error("${archive.path.name} contains an invalid Metadata file: ${se.message}")
                null
            } catch (nfe: NumberFormatException) {
                logger.error("${archive.path.name} contains an invalid Metadata file: ${nfe.message}")
                null
            }
        }

        fun create(archive: BaseArchive): Metadata {
            logger.info("Manually creating Metadata")
            return Metadata(
                issue = Issue(
                    series = Series(
                        publisher = TitledResource(title = Console.prompt(prompt = "Publisher title")!!),
                        title = Console.prompt(prompt = "Series title")!!,
                    ),
                    number = Console.prompt(prompt = "Issue number")!!,
                    pageCount = archive.listFilenames().filter { it.endsWith(".jpg") }.size,
                ),
                meta = Meta(date = LocalDate.now().toKotlinLocalDate(), tool = Tool(value = "Manual")),
            )
        }
    }
}
