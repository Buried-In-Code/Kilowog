package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.archives.BaseArchive
import github.buriedincode.kilowog.models.metroninfo.AgeRating
import github.buriedincode.kilowog.models.metroninfo.Arc
import github.buriedincode.kilowog.models.metroninfo.Credit
import github.buriedincode.kilowog.models.metroninfo.GenreResource
import github.buriedincode.kilowog.models.metroninfo.Gtin
import github.buriedincode.kilowog.models.metroninfo.InformationList
import github.buriedincode.kilowog.models.metroninfo.Page
import github.buriedincode.kilowog.models.metroninfo.Price
import github.buriedincode.kilowog.models.metroninfo.Resource
import github.buriedincode.kilowog.models.metroninfo.Series
import github.buriedincode.kilowog.models.metroninfo.Source
import github.buriedincode.kilowog.models.metroninfo.Universe
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.writeText

@Serializable
class MetronInfo(
    @XmlSerialName("ID")
    var id: InformationList<Source>? = null,
    @XmlSerialName("Publisher")
    val publisher: Resource,
    @XmlSerialName("Series")
    val series: Series,
    @XmlSerialName("CollectionTitle")
    var title: String? = null,
    @XmlSerialName("Number")
    var number: String? = null,
    @XmlSerialName("Stories")
    @XmlChildrenName("Story")
    val stories: List<Resource> = emptyList(),
    @XmlSerialName("Summary")
    var summary: String? = null,
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Prices")
    @XmlChildrenName("Price")
    val prices: List<Price> = emptyList(),
    @XmlSerialName("CoverDate")
    var coverDate: LocalDate? = null,
    @XmlSerialName("StoreDate")
    var storeDate: LocalDate? = null,
    @XmlSerialName("PageCount")
    val pageCount: Int = 0,
    @XmlSerialName("Genres")
    @XmlChildrenName("Genre")
    val genres: List<GenreResource> = emptyList(),
    @XmlSerialName("Tags")
    @XmlChildrenName("Tag")
    val tags: List<Resource> = emptyList(),
    @XmlSerialName("Arcs")
    @XmlChildrenName("Arc")
    var arcs: List<Arc> = emptyList(),
    @XmlSerialName("Characters")
    @XmlChildrenName("Character")
    var characters: List<Resource> = emptyList(),
    @XmlSerialName("Teams")
    @XmlChildrenName("Team")
    var teams: List<Resource> = emptyList(),
    @XmlSerialName("Universes")
    @XmlChildrenName("Universe")
    var universes: List<Universe> = emptyList(),
    @XmlSerialName("Locations")
    @XmlChildrenName("Location")
    var locations: List<Resource> = emptyList(),
    @XmlSerialName("GTIN")
    val gtin: Gtin? = null,
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("Reprints")
    @XmlChildrenName("Reprint")
    val reprints: List<Resource> = emptyList(),
    @XmlSerialName("URL")
    val url: InformationList<String>? = null,
    @XmlSerialName("Credits")
    @XmlChildrenName("Credit")
    var credits: List<Credit> = emptyList(),
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    val pages: List<Page> = emptyList(),
) : InfoModel(), Comparable<MetronInfo> {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Metron-Project/metroninfo/master/drafts/v1.0/MetronInfo.xsd"

    override fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun compareTo(other: MetronInfo): Int = comparator.compare(this, other)

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

    companion object : InfoModel.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        private val comparator = compareBy(MetronInfo::publisher, MetronInfo::series, MetronInfo::number)

        @OptIn(ExperimentalSerializationApi::class)
        override fun fromArchive(archive: BaseArchive): MetronInfo? {
            return try {
                (archive.readFile(filename = "/MetronInfo.xml") ?: archive.readFile(filename = "MetronInfo.xml"))?.let {
                    Utils.XML_MAPPER.decodeFromString(it)
                }
            } catch (mfe: MissingFieldException) {
                LOGGER.error { "${archive.path.name} contains an invalid MetronInfo: ${mfe.message}" }
                null
            } catch (se: SerializationException) {
                LOGGER.error { "${archive.path.name} contains an invalid MetronInfo: ${se.message}" }
                null
            } catch (nfe: NumberFormatException) {
                LOGGER.error { "${archive.path.name} contains an invalid MetronInfo: ${nfe.message}" }
                null
            } catch (nsee: NoSuchElementException) {
                LOGGER.error { "${archive.path.name} contains an invalid MetronInfo: ${nsee.message}" }
                null
            }
        }
    }
}
