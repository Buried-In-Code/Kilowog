package github.comiccorps.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Series(
    @XmlSerialName("Format")
    val format: Format? = null,
    @XmlElement(false)
    var id: Long? = null,
    @XmlElement(false)
    val lang: String = "en",
    @XmlSerialName("Name")
    var name: String,
    @XmlSerialName("SortName")
    val sortName: String? = null,
    @XmlSerialName("Volume")
    val volume: Int? = null,
) : Comparable<Series> {
    override fun compareTo(other: Series): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Series

        if (format != other.format) return false
        if (lang != other.lang) return false
        if (name != other.name) return false
        if (volume != other.volume) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format?.hashCode() ?: 0
        result = 31 * result + lang.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (volume ?: 0)
        return result
    }

    override fun toString(): String {
        return "Series(format=$format, id=$id, lang='$lang', name='$name', sortName=$sortName, volume=$volume)"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, Series::name)
            .thenBy(nullsFirst(), Series::volume)
            .thenBy(nullsFirst(), Series::format)
    }
}
