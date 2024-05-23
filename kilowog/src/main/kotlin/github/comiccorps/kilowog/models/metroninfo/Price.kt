package github.comiccorps.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Price(
    @XmlElement(false)
    val country: String,
    @XmlValue
    val value: Double,
) : Comparable<Price> {
    override fun compareTo(other: Price): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Price

        return country == other.country
    }

    override fun hashCode(): Int {
        return country.hashCode()
    }

    override fun toString(): String {
        return "Price(country='$country', value=$value)"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, Price::country)
    }
}
