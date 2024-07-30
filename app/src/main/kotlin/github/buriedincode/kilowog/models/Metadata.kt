package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.archives.BaseArchive
import github.buriedincode.kilowog.models.metadata.Issue
import github.buriedincode.kilowog.models.metadata.Meta
import github.buriedincode.kilowog.models.metadata.Page
import github.buriedincode.kilowog.models.metadata.Resource
import github.buriedincode.kilowog.models.metadata.Source
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.writeText

@Serializable
class Metadata(
    @XmlSerialName("Issue")
    var issue: Issue,
    @XmlSerialName("Meta")
    var meta: Meta,
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
) : InfoModel(), Comparable<Metadata> {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    var schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Schemas/main/drafts/v1.0/Metadata.xsd"

    override fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun compareTo(other: Metadata): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        return issue == other.issue
    }

    override fun hashCode(): Int {
        return issue.hashCode()
    }

    override fun toString(): String {
        return "Metadata(issue=$issue, meta=$meta, notes=$notes, pages=$pages)"
    }

    companion object : InfoModel.Companion() {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }

        private val comparator = compareBy(Metadata::issue)

        @OptIn(ExperimentalSerializationApi::class)
        override fun fromArchive(archive: BaseArchive): Metadata? {
            return try {
                (archive.readFile(filename = "/Metadata.xml") ?: archive.readFile(filename = "Metadata.xml"))?.let {
                    Utils.XML_MAPPER.decodeFromString(it)
                }
            } catch (mfe: MissingFieldException) {
                LOGGER.error { "${archive.path.name} contains an invalid Metadata: ${mfe.message}" }
                null
            } catch (se: SerializationException) {
                LOGGER.error { "${archive.path.name} contains an invalid Metadata: ${se.message}" }
                null
            } catch (nfe: NumberFormatException) {
                LOGGER.error { "${archive.path.name} contains an invalid Metadata: ${nfe.message}" }
                null
            } catch (nsee: NoSuchElementException) {
                LOGGER.error { "${archive.path.name} contains an invalid Metadata: ${nsee.message}" }
                null
            }
        }
    }
}

internal fun getMetadataId(resources: List<Resource>, source: Source): Long? {
    return resources.firstOrNull { it.source == source }?.value
}
