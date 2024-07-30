package github.buriedincode.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
class InformationList<T>(
    @XmlSerialName("Primary")
    val primary: T,
    @XmlSerialName("Alternative")
    val alternative: List<T> = emptyList(),
)
