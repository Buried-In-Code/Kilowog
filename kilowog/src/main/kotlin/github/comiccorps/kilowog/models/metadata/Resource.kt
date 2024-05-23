package github.comiccorps.kilowog.models.metadata

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Resource(
    @XmlElement(false)
    var source: Source,
    @XmlValue
    var value: Long,
) : Comparable<Resource> {
    override fun compareTo(other: Resource): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Resource) return false

        return source == other.source
    }

    override fun hashCode(): Int {
        return source.hashCode()
    }

    override fun toString(): String {
        return "Resource(source=$source, value=$value)"
    }

    companion object : Logging {
        private val comparator = compareBy(Resource::source)
    }
}
