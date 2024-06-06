package github.comiccorps.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Resource(
    @XmlElement(false)
    var id: Long? = null,
    @XmlValue
    var value: String,
) : Comparable<Resource> {
    override fun compareTo(other: Resource): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Resource(id=$id, value='$value')"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, Resource::value)
    }
}
