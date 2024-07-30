package github.buriedincode.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
class Universe(
    @XmlElement(false)
    val id: Long? = null,
    @XmlSerialName("Name")
    val name: String,
    @XmlSerialName("Number")
    val number: Int? = null,
) : Comparable<Universe> {
    override fun compareTo(other: Universe): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Universe

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "Universe(id=$id, name='$name', number=$number)"
    }

    companion object {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, Universe::name)
    }
}
