package github.buriedincode.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
class Source(
    @XmlElement(false)
    val source: InformationSource,
    @XmlValue
    val value: Long,
) : Comparable<Source> {
    override fun compareTo(other: Source): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Source

        return source == other.source
    }

    override fun hashCode(): Int {
        return source.hashCode()
    }

    override fun toString(): String {
        return "Source(source=$source, value=$value)"
    }

    companion object {
        private val comparator = compareBy(Source::source)
    }
}
