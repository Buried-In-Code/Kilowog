package github.comiccorps.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class Gtin(
    @XmlSerialName("ISBN")
    val isbn: String? = null,
    @XmlSerialName("UPC")
    val upc: String? = null,
)
