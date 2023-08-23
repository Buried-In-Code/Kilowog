package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.Utils.titleCase
import github.buriedincode.kilowog.models.metroninfo.enums.AgeRating
import github.buriedincode.kilowog.models.metroninfo.enums.Format
import github.buriedincode.kilowog.models.metroninfo.enums.Genre
import github.buriedincode.kilowog.models.metroninfo.enums.InformationSource
import github.buriedincode.kilowog.models.metroninfo.enums.PageType
import github.buriedincode.kilowog.models.metroninfo.enums.Role
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import java.nio.file.Path
import kotlin.io.path.writeText
import github.buriedincode.kilowog.models.metadata.enums.Source as MetadataSource

@Serializable
data class MetronInfo(
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("Arcs")
    @XmlChildrenName("Arc")
    val arcs: List<Arc> = emptyList(),
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: Boolean = false,
    @XmlSerialName("Characters")
    @XmlChildrenName("Character")
    val characters: List<Resource> = emptyList(),
    @XmlSerialName("CoverDate")
    val coverDate: LocalDate,
    @XmlSerialName("Credits")
    @XmlChildrenName("Credit")
    val credits: List<Credit> = emptyList(),
    @XmlSerialName("Genres")
    @XmlChildrenName("Genre")
    val genres: List<GenreResource> = emptyList(),
    @XmlSerialName("GTIN")
    val gtin: Gtin? = null,
    @XmlSerialName("ID")
    val id: Source? = null,
    @XmlSerialName("Locations")
    @XmlChildrenName("Location")
    val locations: List<Resource> = emptyList(),
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Number")
    val number: String? = null,
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
    val storeDate: LocalDate? = null,
    @XmlSerialName("Stories")
    @XmlChildrenName("Story")
    val stories: List<Resource> = emptyList(),
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Tags")
    @XmlChildrenName("Tag")
    val tags: List<Resource> = emptyList(),
    @XmlSerialName("Teams")
    @XmlChildrenName("Team")
    val teams: List<Resource> = emptyList(),
    @XmlSerialName("CollectionTitle")
    val title: String? = null,
    @XmlSerialName("URL")
    val url: String? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Metron-Project/metroninfo/master/drafts/v1.0/MetronInfo.xsd"

    @Serializable
    data class Source(
        @XmlElement(false)
        val source: InformationSource,
        @XmlValue
        val value: Int,
    )

    @Serializable
    data class Resource(
        @XmlElement(false)
        val id: Int? = null,
        @XmlValue
        val value: String,
    )

    @Serializable
    data class Series(
        @XmlSerialName("Format")
        val format: Format? = null,
        @XmlElement(false)
        val id: Int? = null,
        @XmlElement(false)
        val lang: String = "en",
        @XmlSerialName("Name")
        val name: String,
        @XmlSerialName("SortName")
        val sortName: String? = null,
        @XmlSerialName("Volume")
        val volume: Int? = null,
    )

    @Serializable
    data class Price(
        @XmlElement(false)
        val country: String,
        @XmlValue
        val value: Double,
    )

    @Serializable
    data class GenreResource(
        @XmlElement(false)
        val id: Int? = null,
        @XmlValue
        val value: Genre,
    )

    @Serializable
    data class Arc(
        @XmlElement(false)
        val id: Int? = null,
        @XmlSerialName("Name")
        val name: String,
        @XmlSerialName("Number")
        val number: Int? = null,
    )

    @Serializable
    data class Gtin(
        @XmlSerialName("ISBN")
        val isbn: String? = null,
        @XmlSerialName("UPC")
        val upc: String? = null,
    )

    @Serializable
    data class Credit(
        @XmlSerialName("Creator")
        val creator: Resource,
        @XmlSerialName("Roles")
        @XmlChildrenName("Role")
        val roles: List<RoleResource> = emptyList(),
    )

    @Serializable
    data class RoleResource(
        @XmlElement(false)
        val id: Int? = null,
        @XmlValue
        val value: Role,
    )

    @Serializable
    data class Page(
        @XmlElement(false)
        val bookmark: String? = null,
        @XmlElement(false)
        val doublePage: Boolean = false,
        @XmlElement(false)
        val image: Int,
        @XmlElement(false)
        val imageHeight: Int? = null,
        @XmlElement(false)
        val imageSize: Long? = null,
        @XmlElement(false)
        val imageWidth: Int? = null,
        @XmlElement(false)
        val key: String? = null,
        @XmlElement(false)
        val type: PageType = PageType.STORY,
    )

    fun toMetadata(): Metadata {
        val source: MetadataSource? = id?.source?.name?.asEnumOrNull<MetadataSource>()
        return Metadata(
            issue = Metadata.Issue(
                characters = this.characters.map { resource ->
                    Metadata.Issue.NamedResource(
                        name = resource.value,
                        resources = listOfNotNull(
                            source?.let {
                                Metadata.Issue.Resource(source = it, value = resource.id ?: return@let null)
                            },
                        ),
                    )
                },
                coverDate = this.coverDate,
                credits = this.credits.map { credit ->
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(
                            name = credit.creator.value,
                            resources = listOfNotNull(
                                source?.let {
                                    Metadata.Issue.Resource(source = it, value = credit.creator.id ?: return@let null)
                                },
                            ),
                        ),
                        roles = credit.roles.map { it.value.titleCase() },
                    )
                },
                genres = this.genres.map {
                    it.value.titleCase()
                },
                language = this.series.lang,
                locations = this.locations.map { location ->
                    Metadata.Issue.NamedResource(
                        name = location.value,
                        resources = listOfNotNull(
                            source?.let {
                                Metadata.Issue.Resource(source = it, value = location.id ?: return@let null)
                            },
                        ),
                    )
                },
                number = this.number,
                pageCount = this.pageCount,
                publisher = Metadata.Issue.Publisher(
                    resources = listOfNotNull(
                        source?.let {
                            Metadata.Issue.Resource(source = it, value = publisher.id ?: return@let null)
                        },
                    ),
                    title = this.publisher.value,
                ),
                resources = listOfNotNull(
                    source?.let {
                        this.id?.let { id ->
                            Metadata.Issue.Resource(source = source, value = id.value)
                        }
                    },
                ),
                series = Metadata.Issue.Series(
                    format = this.series.format?.titleCase()?.asEnumOrNull<github.buriedincode.kilowog.models.metadata.enums.Format>()
                        ?: github.buriedincode.kilowog.models.metadata.enums.Format.COMIC,
                    resources = listOfNotNull(
                        source?.let {
                            Metadata.Issue.Resource(source = it, value = series.id ?: return@let null)
                        },
                    ),
                    title = this.series.name,
                    volume = this.series.volume ?: 1,
                ),
                storeDate = this.storeDate,
                storyArcs = this.arcs.map { arc ->
                    Metadata.Issue.StoryArc(
                        number = arc.number,
                        resources = listOfNotNull(
                            source?.let {
                                Metadata.Issue.Resource(source = it, value = arc.id ?: return@let null)
                            },
                        ),
                        title = arc.name,
                    )
                },
                summary = this.summary,
                teams = this.teams.map { team ->
                    Metadata.Issue.NamedResource(
                        name = team.value,
                        resources = listOfNotNull(
                            source?.let {
                                Metadata.Issue.Resource(source = it, value = team.id ?: return@let null)
                            },
                        ),
                    )
                },
                title = this.title,
            ),
            notes = this.notes,
            pages = this.pages.map {
                Metadata.Page(
                    doublePage = it.doublePage,
                    filename = "",
                    fileSize = it.imageSize ?: 0L,
                    imageHeight = it.imageHeight ?: 0,
                    imageWidth = it.imageWidth ?: 0,
                    index = it.image,
                    type = it.type.titleCase(),
                )
            },
        )
    }

    fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }
}
