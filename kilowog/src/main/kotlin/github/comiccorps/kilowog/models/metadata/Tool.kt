package github.comiccorps.kilowog.models.metadata

import github.comiccorps.kilowog.Utils
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class Tool(
    @XmlValue
    val value: String = "Kilowog",
    @XmlElement(false)
    val version: String = Utils.VERSION,
)
