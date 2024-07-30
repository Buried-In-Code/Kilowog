package github.buriedincode.kilowog.models.metadata

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
class Issue(
    @XmlSerialName("Characters")
    @XmlChildrenName("Character")
    var characters: List<TitledResource> = emptyList(),
    @XmlSerialName("CoverDate")
    var coverDate: LocalDate? = null,
    @XmlSerialName("Credits")
    @XmlChildrenName("Credit")
    var credits: List<Credit> = emptyList(),
    @XmlSerialName("Format")
    var format: Format = Format.SINGLE_ISSUE,
    @XmlElement(false)
    var language: String = "en",
    @XmlSerialName("Locations")
    @XmlChildrenName("Location")
    var locations: List<TitledResource> = emptyList(),
    @XmlSerialName("Number")
    var number: String? = null,
    @XmlSerialName("PageCount")
    var pageCount: Int,
    @XmlSerialName("Resources")
    @XmlChildrenName("Resource")
    var resources: List<Resource> = emptyList(),
    @XmlSerialName("Series")
    var series: Series,
    @XmlSerialName("StoreDate")
    var storeDate: LocalDate? = null,
    @XmlSerialName("StoryArcs")
    @XmlChildrenName("StoryArc")
    var storyArcs: List<StoryArc> = emptyList(),
    @XmlSerialName("Summary")
    var summary: String? = null,
    @XmlSerialName("Teams")
    @XmlChildrenName("Team")
    var teams: List<TitledResource> = emptyList(),
    @XmlSerialName("Title")
    var title: String? = null,
) : Comparable<Issue> {
//    fun getFilename(): String {
//        val seriesTitle = if (series.volume == 1) series.title else "${series.title} v${series.volume}"
//        val issueTitle = if (number != null) {
//            "_#${number!!.padStart(if (format == Format.COMIC) 3 else 2, '0')}"
//        } else if (title != null) {
//            "_$title"
//        } else {
//            ""
//        }
//        val issueFormat = when (format) {
//            Format.ANNUAL -> "_Annual"
//            Format.DIGITAL_CHAPTER -> "_Chapter"
//            Format.GRAPHIC_NOVEL -> "_GN"
//            Format.HARDCOVER -> "_HC"
//            Format.TRADE_PAPERBACK -> "_TP"
//            else -> ""
//        }
//        return Utils.sanitize(
//            value = when (format) {
//                Format.ANNUAL, Format.DIGITAL_CHAPTER -> seriesTitle + issueFormat + issueTitle
//                Format.GRAPHIC_NOVEL, Format.HARDCOVER, Format.TRADE_PAPERBACK -> seriesTitle + issueTitle + issueFormat
//                else -> seriesTitle + issueTitle
//            },
//        )
//    }

    override fun compareTo(other: Issue): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Issue

        if (format != other.format) return false
        if (language != other.language) return false
        if (number != other.number) return false
        if (series != other.series) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + (number?.hashCode() ?: 0)
        result = 31 * result + series.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Issue(" +
            "characters=$characters, " +
            "coverDate=$coverDate, " +
            "credits=$credits, " +
            "format=$format, " +
            "language='$language', " +
            "locations=$locations, " +
            "number=$number, " +
            "pageCount=$pageCount, " +
            "resources=$resources, " +
            "series=$series, " +
            "storeDate=$storeDate, " +
            "storyArcs=$storyArcs, " +
            "summary=$summary, " +
            "teams=$teams, " +
            "title=$title" +
            ")"
    }

    companion object {
        private val comparator = compareBy(Issue::series)
            .thenBy(nullsLast()) { it.number?.toIntOrNull() }
            .thenBy(Issue::number)
            .thenBy(nullsLast(), Issue::title)
    }
}
