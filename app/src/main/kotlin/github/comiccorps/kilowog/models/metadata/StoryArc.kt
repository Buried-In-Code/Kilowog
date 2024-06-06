package github.comiccorps.kilowog.models.metadata

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class StoryArc(
    @XmlSerialName("Number")
    var number: Int? = null,
    @XmlSerialName("Resources")
    @XmlChildrenName("Resource")
    var resources: List<Resource> = emptyList(),
    @XmlSerialName("Title")
    var title: String,
) : Comparable<StoryArc> {
    override fun compareTo(other: StoryArc): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryArc

        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }

    override fun toString(): String {
        return "StoryArc(number=$number, resources=$resources, title='$title')"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, StoryArc::title)
    }
}
