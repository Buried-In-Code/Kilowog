package github.comiccorps.kilowog.services.comicvine.issue

import github.comiccorps.kilowog.LocalDateTimeSerializer
import github.comiccorps.kilowog.services.comicvine.AssociatedImage
import github.comiccorps.kilowog.services.comicvine.GenericEntry
import github.comiccorps.kilowog.services.comicvine.Image
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssueEntry(
    val aliases: String? = null,
    val associatedImages: List<AssociatedImage> = emptyList(),
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val coverDate: LocalDate? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateAdded: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateLastUpdated: LocalDateTime,
    val description: String? = null,
    val hasStaffReview: Boolean,
    val id: Long,
    val image: Image,
    val name: String? = null,
    @JsonNames("issue_number")
    val number: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    val storeDate: LocalDate? = null,
    @JsonNames("deck")
    val summary: String? = null,
    val volume: GenericEntry,
) : Comparable<IssueEntry> {
    companion object {
        private val comparator = compareBy(IssueEntry::number).thenBy { it.name }
    }

    override fun compareTo(other: IssueEntry): Int = comparator.compare(this, other)
}
