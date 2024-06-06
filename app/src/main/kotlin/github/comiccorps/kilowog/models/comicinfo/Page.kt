package github.comiccorps.kilowog.models.comicinfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Page(
    @XmlSerialName("Image")
    @XmlElement(false)
    val image: Int,
    @XmlSerialName("Type")
    @XmlElement(false)
    val type: PageType = PageType.STORY,
    @XmlSerialName("DoublePage")
    @XmlElement(false)
    val doublePage: Boolean = false,
    @XmlSerialName("ImageSize")
    @XmlElement(false)
    val imageSize: Long? = null,
    @XmlSerialName("Key")
    @XmlElement(false)
    val key: String? = null,
    @XmlSerialName("Bookmark")
    @XmlElement(false)
    val bookmark: String? = null,
    @XmlSerialName("ImageWidth")
    @XmlElement(false)
    val imageWidth: Int? = null,
    @XmlSerialName("ImageHeight")
    @XmlElement(false)
    val imageHeight: Int? = null,
) : Comparable<Page> {
    override fun compareTo(other: Page): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Page

        return image == other.image
    }

    override fun hashCode(): Int {
        return image
    }

    override fun toString(): String {
        return "Page(" +
            "bookmark=$bookmark, " +
            "doublePage=$doublePage, " +
            "image=$image, " +
            "imageHeight=$imageHeight, " +
            "imageSize=$imageSize, " +
            "imageWidth=$imageWidth, " +
            "key=$key, " +
            "type=$type" +
            ")"
    }

    companion object : Logging {
        private val comparator = compareBy(Page::image)
    }
}
