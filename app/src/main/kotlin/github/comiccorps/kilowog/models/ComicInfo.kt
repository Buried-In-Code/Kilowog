package github.comiccorps.kilowog.models

import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.Utils.asEnumOrNull
import github.comiccorps.kilowog.archives.BaseArchive
import github.comiccorps.kilowog.models.comicinfo.AgeRating
import github.comiccorps.kilowog.models.comicinfo.Manga
import github.comiccorps.kilowog.models.comicinfo.Page
import github.comiccorps.kilowog.models.comicinfo.YesNo
import github.comiccorps.kilowog.models.metadata.Credit
import github.comiccorps.kilowog.models.metadata.Format
import github.comiccorps.kilowog.models.metadata.Issue
import github.comiccorps.kilowog.models.metadata.Meta
import github.comiccorps.kilowog.models.metadata.PageType
import github.comiccorps.kilowog.models.metadata.Series
import github.comiccorps.kilowog.models.metadata.StoryArc
import github.comiccorps.kilowog.models.metadata.TitledResource
import github.comiccorps.kilowog.models.metadata.Tool
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
import github.comiccorps.kilowog.models.metadata.Page as MetadataPage

@Serializable
class ComicInfo(
    @XmlSerialName("AgeRating")
    var ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("AlternateCount")
    var alternateCount: Int? = null,
    @XmlSerialName("AlternateNumber")
    var alternateNumber: String? = null,
    @XmlSerialName("AlternateSeries")
    var alternateSeries: String? = null,
    @XmlSerialName("BlackAndWhite")
    var blackAndWhite: YesNo = YesNo.UNKNOWN,
    @XmlSerialName("Characters")
    var characters: String? = null,
    @XmlSerialName("Colorist")
    var colorist: String? = null,
    @XmlSerialName("CommunityRating")
    var communityRating: Double? = null,
    @XmlSerialName("Count")
    var count: Int? = null,
    @XmlSerialName("CoverArtist")
    var coverArtist: String? = null,
    @XmlSerialName("Day")
    var day: Int? = null,
    @XmlSerialName("Editor")
    var editor: String? = null,
    @XmlSerialName("Format")
    var format: String? = null,
    @XmlSerialName("Genre")
    var genre: String? = null,
    @XmlSerialName("Imprint")
    var imprint: String? = null,
    @XmlSerialName("Inker")
    var inker: String? = null,
    @XmlSerialName("LanguageISO")
    var language: String? = null,
    @XmlSerialName("Letterer")
    var letterer: String? = null,
    @XmlSerialName("Locations")
    var locations: String? = null,
    @XmlSerialName("MainCharacterOrTeam")
    var mainCharacterOrTeam: String? = null,
    @XmlSerialName("Manga")
    var manga: Manga = Manga.UNKNOWN,
    @XmlSerialName("Month")
    var month: Int? = null,
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Number")
    var number: String? = null,
    @XmlSerialName("PageCount")
    var pageCount: Int = 0,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
    @XmlSerialName("Penciller")
    var penciller: String? = null,
    @XmlSerialName("Publisher")
    var publisher: String? = null,
    @XmlSerialName("Review")
    var review: String? = null,
    @XmlSerialName("ScanInformation")
    var scanInformation: String? = null,
    @XmlSerialName("Series")
    var series: String? = null,
    @XmlSerialName("SeriesGroup")
    var seriesGroup: String? = null,
    @XmlSerialName("StoryArc")
    var storyArc: String? = null,
    @XmlSerialName("Summary")
    var summary: String? = null,
    @XmlSerialName("Teams")
    var teams: String? = null,
    @XmlSerialName("Title")
    var title: String? = null,
    @XmlSerialName("Volume")
    var volume: Int? = null,
    @XmlSerialName("Web")
    var web: String? = null,
    @XmlSerialName("Writer")
    var writer: String? = null,
    @XmlSerialName("Year")
    var year: Int? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/ComicCorps/Schemas/main/schemas/v2.0/ComicInfo.xsd"

    var characterList: List<String>
        get() = strToList(value = this.characters)
        set(value) {
            this.characters = listToStr(value = value)
        }

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

    var teamList: List<String>
        get() = strToList(value = this.teams)
        set(value) {
            this.teams = listToStr(value = value)
        }

    fun toMetadata(): Metadata? {
        return Metadata(
            issue = Issue(
                characters = this.characterList.map { TitledResource(title = it) },
                coverDate = this.coverDate,
                credits = this.credits.map { (key, value) ->
                    Credit(
                        creator = TitledResource(title = key),
                        roles = value.map { TitledResource(title = it) },
                    )
                },
                format = this.format?.asEnumOrNull<Format>() ?: Format.COMIC,
                language = this.language ?: "en",
                locations = this.locationList.map { TitledResource(title = it) },
                number = this.number,
                pageCount = this.pageCount,
                // Missing Resources
                series = Series(
                    genres = this.genreList.map { TitledResource(title = it) },
                    publisher = TitledResource(title = this.publisher ?: return null),
                    // Missing Resources
                    startYear = if (this.volume != null && this.volume!! >= 1900) this.volume else null,
                    title = this.series ?: return null,
                    volume = if (this.volume != null && this.volume!! <= 1900) this.volume!! else 1,
                ),
                // Missing Store Date
                storyArcs = this.storyArcList.map { StoryArc(title = it) },
                summary = this.summary,
                teams = this.teamList.map { TitledResource(title = it) },
                title = this.title,
            ),
            meta = Meta(
                date = java.time.LocalDate
                    .now()
                    .toKotlinLocalDate(),
                tool = Tool(value = "ComicInfo"),
            ),
            notes = this.notes,
            pages = this.pages.mapNotNull {
                MetadataPage(
                    doublePage = it.doublePage,
                    filename = "",
                    size = it.imageSize ?: 0L,
                    height = it.imageHeight ?: 0,
                    width = it.imageWidth ?: 0,
                    index = it.image,
                    type = it.type.name.asEnumOrNull<PageType>() ?: return@mapNotNull null,
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

    override fun toString(): String {
        return "ComicInfo(" +
            "ageRating=$ageRating, " +
            "alternateCount=$alternateCount, " +
            "alternateNumber=$alternateNumber, " +
            "blackAndWhite=$blackAndWhite, " +
            "characters=$characters, " +
            "colorist=$colorist, " +
            "communityRating=$communityRating, " +
            "count=$count, " +
            "coverArtist=$coverArtist, " +
            "coverDate=$coverDate, " +
            "editor=$editor, " +
            "format=$format, " +
            "genre=$genre, " +
            "imprint=$imprint, " +
            "inker=$inker, " +
            "language=$language, " +
            "letterer=$letterer, " +
            "locations=$locations, " +
            "mainCharacterOrTeam=$mainCharacterOrTeam, " +
            "manga=$manga, " +
            "notes=$notes, " +
            "number=$number, " +
            "pageCount=$pageCount, " +
            "pages=$pages, " +
            "penciller=$penciller, " +
            "publisher=$publisher, " +
            "review=$review, " +
            "scanInformation=$scanInformation, " +
            "series=$series, " +
            "seriesGroup=$seriesGroup, " +
            "storyArc=$storyArc, " +
            "summary=$summary, " +
            "teams=$teams, " +
            "title=$title, " +
            "volume=$volume, " +
            "web=$web, " +
            "writer=$writer" +
            ")"
    }

    companion object : Logging {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromArchive(archive: BaseArchive): ComicInfo? {
            return try {
                archive.readFile(filename = "/ComicInfo.xml")?.let { Utils.XML_MAPPER.decodeFromString<ComicInfo>(it) }
                    ?: archive.readFile(filename = "ComicInfo.xml")?.let { Utils.XML_MAPPER.decodeFromString<ComicInfo>(it) }
            } catch (mfe: MissingFieldException) {
                logger.error("${archive.path.name} contains an invalid ComicInfo file: ${mfe.message}")
                null
            } catch (se: SerializationException) {
                logger.error("${archive.path.name} contains an invalid ComicInfo file: ${se.message}")
                null
            } catch (nfe: NumberFormatException) {
                logger.error("${archive.path.name} contains an invalid ComicInfo file: ${nfe.message}")
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
