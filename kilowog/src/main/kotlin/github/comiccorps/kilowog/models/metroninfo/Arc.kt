package github.comiccorps.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Arc(
    @XmlElement(false)
    val id: Long? = null,
    @XmlSerialName("Name")
    val name: String,
    @XmlSerialName("Number")
    val number: Int? = null,
) : Comparable<Arc> {
    override fun compareTo(other: Arc): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Arc

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "Arc(id=$id, name='$name', number=$number)"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, Arc::name)
    }
}
