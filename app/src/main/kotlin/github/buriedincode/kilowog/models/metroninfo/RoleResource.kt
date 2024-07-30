package github.buriedincode.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
class RoleResource(
    @XmlElement(false)
    val id: Long? = null,
    @XmlValue
    val value: Role,
) : Comparable<RoleResource> {
    override fun compareTo(other: RoleResource): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoleResource

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "RoleResource(id=$id, value=$value)"
    }

    companion object {
        private val comparator = compareBy(RoleResource::value)
    }
}
