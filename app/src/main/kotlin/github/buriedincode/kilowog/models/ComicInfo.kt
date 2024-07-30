package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.archives.BaseArchive
import github.buriedincode.kilowog.models.comicinfo.AgeRating
import github.buriedincode.kilowog.models.comicinfo.Manga
import github.buriedincode.kilowog.models.comicinfo.Page
import github.buriedincode.kilowog.models.comicinfo.YesNo
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
class ComicInfo(
    @XmlSerialName("Title")
    var title: String? = null,
    @XmlSerialName("Series")
    var series: String? = null,
    @XmlSerialName("Number")
    var number: String? = null,
    @XmlSerialName("Count")
    var count: Int? = null,
    @XmlSerialName("Volume")
    var volume: Int? = null,
    @XmlSerialName("AlternateSeries")
    var alternateSeries: String? = null,
    @XmlSerialName("AlternateNumber")
    var alternateNumber: String? = null,
    @XmlSerialName("AlternateCount")
    var alternateCount: Int? = null,
    @XmlSerialName("Summary")
    var summary: String? = null,
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Year")
    var year: Int? = null,
    @XmlSerialName("Month")
    var month: Int? = null,
    @XmlSerialName("Day")
    var day: Int? = null,
    @XmlSerialName("Writer")
    var writer: String? = null,
    @XmlSerialName("Penciller")
    var penciller: String? = null,
    @XmlSerialName("Inker")
    var inker: String? = null,
    @XmlSerialName("Colorist")
    var colorist: String? = null,
    @XmlSerialName("Letterer")
    var letterer: String? = null,
    @XmlSerialName("CoverArtist")
    var coverArtist: String? = null,
    @XmlSerialName("Editor")
    var editor: String? = null,
    @XmlSerialName("Publisher")
    var publisher: String? = null,
    @XmlSerialName("Imprint")
    var imprint: String? = null,
    @XmlSerialName("Genre")
    var genre: String? = null,
    @XmlSerialName("Web")
    var web: String? = null,
    @XmlSerialName("PageCount")
    var pageCount: Int = 0,
    @XmlSerialName("LanguageISO")
    var language: String? = null,
    @XmlSerialName("Format")
    var format: String? = null,
    @XmlSerialName("BlackAndWhite")
    var blackAndWhite: YesNo = YesNo.UNKNOWN,
    @XmlSerialName("Manga")
    var manga: Manga = Manga.UNKNOWN,
    @XmlSerialName("Characters")
    var characters: String? = null,
    @XmlSerialName("Teams")
    var teams: String? = null,
    @XmlSerialName("Locations")
    var locations: String? = null,
    @XmlSerialName("ScanInformation")
    var scanInformation: String? = null,
    @XmlSerialName("StoryArc")
    var storyArc: String? = null,
    @XmlSerialName("SeriesGroup")
    var seriesGroup: String? = null,
    @XmlSerialName("AgeRating")
    var ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
    @XmlSerialName("CommunityRating")
    var communityRating: Double? = null,
    @XmlSerialName("MainCharacterOrTeam")
    var mainCharacterOrTeam: String? = null,
    @XmlSerialName("Review")
    var review: String? = null,
) : InfoModel(), Comparable<ComicInfo> {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Schemas/main/schemas/v2.0/ComicInfo.xsd"

    var coverDate: LocalDate?
        get() = this.year?.let {
            LocalDate(year = it, monthNumber = this.month ?: 1, dayOfMonth = this.day ?: 1)
        }
        set(value) {
            this.year = value?.year
            this.month = value?.monthNumber
            this.day = value?.dayOfMonth
        }

    var credits: Map<String, List<String>>
        get() {
            val output = mutableMapOf<String, MutableList<String>>()
            mapOf(
                "Writer" to this.writer,
                "Penciller" to this.penciller,
                "Inker" to this.inker,
                "Colorist" to this.colorist,
                "Letterer" to this.letterer,
                "Cover Artist" to this.coverArtist,
                "Editor" to this.editor,
            ).forEach { (role, attribute) ->
                strToList(value = attribute).forEach {
                    output.putIfAbsent(it, mutableListOf())
                    output[it]!!.add(role)
                }
            }
            return output
        }
        set(value) {
            this.writer = listToStr(value = listCreators(role = "Writer", mapping = value))
            this.penciller = listToStr(value = listCreators(role = "Penciller", mapping = value))
            this.inker = listToStr(value = listCreators(role = "Inker", mapping = value))
            this.colorist = listToStr(value = listCreators(role = "Colorist", mapping = value))
            this.letterer = listToStr(value = listCreators(role = "Letterer", mapping = value))
            this.coverArtist = listToStr(value = listCreators(role = "Cover Artist", mapping = value))
            this.editor = listToStr(value = listCreators(role = "Editor", mapping = value))
        }

    var genreList: List<String>
        get() = strToList(value = this.genre)
        set(value) {
            this.genre = listToStr(value = value)
        }

    var characterList: List<String>
        get() = strToList(value = this.characters)
        set(value) {
            this.characters = listToStr(value = value)
        }

    var teamList: List<String>
        get() = strToList(value = this.teams)
        set(value) {
            this.teams = listToStr(value = value)
        }

    var locationList: List<String>
        get() = strToList(value = this.locations)
        set(value) {
            this.locations = listToStr(value = value)
        }

    var storyArcList: List<String>
        get() = strToList(value = this.storyArc)
        set(value) {
            this.storyArc = listToStr(value = value)
        }

    override fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun compareTo(other: ComicInfo): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComicInfo

        if (format != other.format) return false
        if (imprint != other.imprint) return false
        if (language != other.language) return false
        if (number != other.number) return false
        if (publisher != other.publisher) return false
        if (series != other.series) return false
        if (title != other.title) return false
        if (volume != other.volume) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format?.hashCode() ?: 0
        result = 31 * result + (imprint?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (number?.hashCode() ?: 0)
        result = 31 * result + (publisher?.hashCode() ?: 0)
        result = 31 * result + (series?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (volume ?: 0)
        return result
    }

    companion object : InfoModel.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        private val comparator = compareBy(ComicInfo::publisher, ComicInfo::series, ComicInfo::number)

        @OptIn(ExperimentalSerializationApi::class)
        override fun fromArchive(archive: BaseArchive): ComicInfo? {
            return try {
                (archive.readFile(filename = "/ComicInfo.xml") ?: archive.readFile(filename = "ComicInfo.xml"))?.let {
                    Utils.XML_MAPPER.decodeFromString(it)
                }
            } catch (mfe: MissingFieldException) {
                LOGGER.error { "${archive.path.name} contains an invalid ComicInfo: ${mfe.message}" }
                null
            } catch (se: SerializationException) {
                LOGGER.error { "${archive.path.name} contains an invalid ComicInfo: ${se.message}" }
                null
            } catch (nfe: NumberFormatException) {
                LOGGER.error { "${archive.path.name} contains an invalid ComicInfo: ${nfe.message}" }
                null
            } catch (nsee: NoSuchElementException) {
                LOGGER.error { "${archive.path.name} contains an invalid ComicInfo: ${nsee.message}" }
                null
            }
        }

        private fun strToList(value: String?): List<String> {
            return value?.split(",")?.map { it.replace("\"", "").trim() }?.sorted() ?: emptyList()
        }

        private fun listToStr(value: List<String>): String? {
            if (value.isEmpty()) {
                return null
            }
            return value.joinToString(",") { if ("," in it) "\"$it\"" else it }
        }

        private fun listCreators(role: String, mapping: Map<String, List<String>>): List<String> {
            return mapping.entries
                .filter { entry ->
                    entry.value.any { it.equals(role, ignoreCase = true) }
                }.map { it.key }
                .toSet()
                .sorted()
        }
    }
}
