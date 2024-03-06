package github.comiccorps.kilowog.models.metadata

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class NamedResource(
    @XmlSerialName("Resources")
    @XmlChildrenName("Resource")
    var resources: List<Resource> = emptyList(),
    @XmlSerialName("Title")
    var title: String,
) : Comparable<NamedResource> {
    override fun compareTo(other: NamedResource): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NamedResource) return false

        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }

    override fun toString(): String {
        return "NamedResource(title=$title, resources=$resources)"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, NamedResource::title)
    }
}
