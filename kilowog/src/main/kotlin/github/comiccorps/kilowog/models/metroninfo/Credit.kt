package github.comiccorps.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Credit(
    @XmlSerialName("Creator")
    val creator: Resource,
    @XmlSerialName("Roles")
    @XmlChildrenName("Role")
    val roles: List<RoleResource> = emptyList(),
) : Comparable<Credit> {
    override fun compareTo(other: Credit): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Credit

        return creator == other.creator
    }

    override fun hashCode(): Int {
        return creator.hashCode()
    }

    override fun toString(): String {
        return "Credit(creator=$creator, roles=$roles)"
    }

    companion object : Logging {
        private val comparator = compareBy(Credit::creator)
    }
}
