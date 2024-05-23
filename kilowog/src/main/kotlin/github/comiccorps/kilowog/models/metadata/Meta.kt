package github.comiccorps.kilowog.models.metadata

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class Meta(
    @XmlElement(false)
    val date: LocalDate,
    @XmlSerialName("Tool")
    val tool: Tool = Tool(),
)
