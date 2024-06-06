package github.comiccorps.kilowog.models.metadata

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Page(
    @XmlElement(false)
    var doublePage: Boolean = false,
    @XmlElement(false)
    var filename: String,
    @XmlElement(false)
    var height: Int,
    @XmlElement(false)
    var index: Int,
    @XmlElement(false)
    var size: Long,
    @XmlElement(false)
    var type: PageType = PageType.STORY,
    @XmlElement(false)
    var width: Int,
) : Comparable<Page> {
    override fun compareTo(other: Page): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Page

        return filename == other.filename || index == other.index
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + index
        return result
    }

    override fun toString(): String {
        return "Page(" +
            "doublePage=$doublePage, " +
            "filename='$filename', " +
            "height=$height, " +
            "index=$index, " +
            "size=$size, " +
            "type='$type', " +
            "width=$width" +
            ")"
    }

    companion object : Logging {
        private val comparator = compareBy(Page::index)
    }
}
