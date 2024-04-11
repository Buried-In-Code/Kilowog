package github.comiccorps.kilowog.models

import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.Utils.asEnumOrNull
import github.comiccorps.kilowog.Utils.titlecase
import github.comiccorps.kilowog.archives.BaseArchive
import github.comiccorps.kilowog.models.metadata.Format
import github.comiccorps.kilowog.models.metadata.Issue
import github.comiccorps.kilowog.models.metadata.Meta
import github.comiccorps.kilowog.models.metadata.StoryArc
import github.comiccorps.kilowog.models.metadata.TitledResource
import github.comiccorps.kilowog.models.metadata.Tool
import github.comiccorps.kilowog.models.metroninfo.AgeRating
import github.comiccorps.kilowog.models.metroninfo.Arc
import github.comiccorps.kilowog.models.metroninfo.Credit
import github.comiccorps.kilowog.models.metroninfo.GenreResource
import github.comiccorps.kilowog.models.metroninfo.Gtin
import github.comiccorps.kilowog.models.metroninfo.Page
import github.comiccorps.kilowog.models.metroninfo.Price
import github.comiccorps.kilowog.models.metroninfo.Resource
import github.comiccorps.kilowog.models.metroninfo.Series
import github.comiccorps.kilowog.models.metroninfo.Source
import kotlinx.datetime.LocalDate
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
import kotlin.io.path.name
import kotlin.io.path.writeText
import github.comiccorps.kilowog.models.metadata.Credit as MetadataCredit
import github.comiccorps.kilowog.models.metadata.Page as MetadataPage
import github.comiccorps.kilowog.models.metadata.PageType as MetadataPageType
import github.comiccorps.kilowog.models.metadata.Resource as MetadataResource
import github.comiccorps.kilowog.models.metadata.Series as MetadataSeries
import github.comiccorps.kilowog.models.metadata.Source as MetadataSource

@Serializable
class MetronInfo(
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("Arcs")
    @XmlChildrenName("Arc")
    var arcs: List<Arc> = emptyList(),
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: Boolean = false,
    @XmlSerialName("Characters")
    @XmlChildrenName("Character")
    var characters: List<Resource> = emptyList(),
    @XmlSerialName("CoverDate")
    var coverDate: LocalDate,
    @XmlSerialName("Credits")
    @XmlChildrenName("Credit")
    var credits: List<Credit> = emptyList(),
    @XmlSerialName("Genres")
    @XmlChildrenName("Genre")
    val genres: List<GenreResource> = emptyList(),
    @XmlSerialName("GTIN")
    val gtin: Gtin? = null,
    @XmlSerialName("ID")
    var id: Source? = null,
    @XmlSerialName("Locations")
    @XmlChildrenName("Location")
    var locations: List<Resource> = emptyList(),
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Number")
    var number: String? = null,
    @XmlSerialName("PageCount")
    val pageCount: Int = 0,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    val pages: List<Page> = emptyList(),
    @XmlSerialName("Prices")
    @XmlChildrenName("Price")
    val prices: List<Price> = emptyList(),
    @XmlSerialName("Publisher")
    val publisher: Resource,
    @XmlSerialName("Reprints")
    @XmlChildrenName("Reprint")
    val reprints: List<Resource> = emptyList(),
    @XmlSerialName("Series")
    val series: Series,
    @XmlSerialName("StoreDate")
    var storeDate: LocalDate? = null,
    @XmlSerialName("Stories")
    @XmlChildrenName("Story")
    val stories: List<Resource> = emptyList(),
    @XmlSerialName("Summary")
    var summary: String? = null,
    @XmlSerialName("Tags")
    @XmlChildrenName("Tag")
    val tags: List<Resource> = emptyList(),
    @XmlSerialName("Teams")
    @XmlChildrenName("Team")
    var teams: List<Resource> = emptyList(),
    @XmlSerialName("CollectionTitle")
    var title: String? = null,
    @XmlSerialName("URL")
    val url: String? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Metron-Project/metroninfo/master/drafts/v1.0/MetronInfo.xsd"

    fun toMetadata(): Metadata {
        val source: MetadataSource? = id?.source?.name?.asEnumOrNull<MetadataSource>()
        return Metadata(
            issue = Issue(
                characters = this.characters.map { resource ->
                    TitledResource(
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = resource.id ?: return@let null)
                            },
                        ),
                        title = resource.value,
                    )
                },
                coverDate = this.coverDate,
                credits = this.credits.map { credit ->
                    MetadataCredit(
                        creator = TitledResource(
                            resources = listOfNotNull(
                                source?.let {
                                    MetadataResource(source = it, value = credit.creator.id ?: return@let null)
                                },
                            ),
                            title = credit.creator.value,
                        ),
                        roles = credit.roles.map { role ->
                            TitledResource(
                                resources = listOfNotNull(
                                    source?.let {
                                        MetadataResource(source = it, value = role.id ?: return@let null)
                                    },
                                ),
                                title = role.value.titlecase(),
                            )
                        },
                    )
                },
                format = this.series.format?.titlecase()?.asEnumOrNull<Format>() ?: Format.COMIC,
                language = this.series.lang,
                locations = this.locations.map { location ->
                    TitledResource(
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = location.id ?: return@let null)
                            },
                        ),
                        title = location.value,
                    )
                },
                number = this.number,
                pageCount = this.pageCount,
                resources = listOfNotNull(
                    source?.let {
                        this.id?.let { id ->
                            MetadataResource(source = source, value = id.value)
                        }
                    },
                ),
                series = MetadataSeries(
                    genres = this.genres.map { genre ->
                        TitledResource(
                            resources = listOfNotNull(
                                source?.let {
                                    MetadataResource(source = it, value = genre.id ?: return@let null)
                                },
                            ),
                            title = genre.value.titlecase(),
                        )
                    },
                    publisher = TitledResource(
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = publisher.id ?: return@let null)
                            },
                        ),
                        title = this.publisher.value,
                    ),
                    resources = listOfNotNull(
                        source?.let {
                            MetadataResource(source = it, value = series.id ?: return@let null)
                        },
                    ),
                    title = this.series.name,
                    volume = this.series.volume ?: 1,
                ),
                storeDate = this.storeDate,
                storyArcs = this.arcs.map { arc ->
                    StoryArc(
                        number = arc.number,
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = arc.id ?: return@let null)
                            },
                        ),
                        title = arc.name,
                    )
                },
                summary = this.summary,
                teams = this.teams.map { team ->
                    TitledResource(
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = team.id ?: return@let null)
                            },
                        ),
                        title = team.value,
                    )
                },
                title = this.title,
            ),
            meta = Meta(date = java.time.LocalDate.now().toKotlinLocalDate(), tool = Tool(value = "MetronInfo")),
            notes = this.notes,
            pages = this.pages.mapNotNull {
                MetadataPage(
                    doublePage = it.doublePage,
                    filename = "",
                    size = it.imageSize ?: 0L,
                    height = it.imageHeight ?: 0,
                    width = it.imageWidth ?: 0,
                    index = it.image,
                    type = it.type.name.asEnumOrNull<MetadataPageType>() ?: return@mapNotNull null,
                )
            },
        )
    }

    fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetronInfo

        if (number != other.number) return false
        if (publisher != other.publisher) return false
        if (series != other.series) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number?.hashCode() ?: 0
        result = 31 * result + publisher.hashCode()
        result = 31 * result + series.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MetronInfo(" +
            "ageRating=$ageRating, " +
            "arcs=$arcs, " +
            "blackAndWhite=$blackAndWhite, " +
            "characters=$characters, " +
            "coverDate=$coverDate, " +
            "credits=$credits, " +
            "genres=$genres, " +
            "gtin=$gtin, " +
            "id=$id, " +
            "locations=$locations, " +
            "notes=$notes, " +
            "number=$number, " +
            "pageCount=$pageCount, " +
            "pages=$pages, " +
            "prices=$prices, " +
            "publisher=$publisher, " +
            "reprints=$reprints, " +
            "series=$series, " +
            "storeDate=$storeDate, " +
            "stories=$stories, " +
            "summary=$summary, " +
            "tags=$tags, " +
            "teams=$teams, " +
            "title=$title, " +
            "url=$url, " +
            "schemaUrl='$schemaUrl'" +
            ")"
    }

    companion object : Logging {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromArchive(archive: BaseArchive): MetronInfo? {
            return try {
                archive.readFile(filename = "/MetronInfo.xml")?.let { Utils.XML_MAPPER.decodeFromString<MetronInfo>(it) }
                    ?: archive.readFile(filename = "MetronInfo.xml")?.let { Utils.XML_MAPPER.decodeFromString<MetronInfo>(it) }
            } catch (mfe: MissingFieldException) {
                logger.error("${archive.path.name} contains an invalid MetronInfo file: ${mfe.message}")
                null
            } catch (se: SerializationException) {
                logger.error("${archive.path.name} contains an invalid MetronInfo file: ${se.message}")
                null
            } catch (nfe: NumberFormatException) {
                logger.error("${archive.path.name} contains an invalid MetronInfo file: ${nfe.message}")
                null
            }
        }
    }
}
