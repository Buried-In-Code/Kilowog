package github.comiccorps.kilowog.services.comicvine.volume

import github.comiccorps.kilowog.LocalDateTimeSerializer
import github.comiccorps.kilowog.StartYearSerializer
import github.comiccorps.kilowog.services.comicvine.GenericEntry
import github.comiccorps.kilowog.services.comicvine.Image
import github.comiccorps.kilowog.services.comicvine.IssueEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VolumeEntry(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateAdded: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateLastUpdated: LocalDateTime,
    val description: String? = null,
    val firstIssue: IssueEntry? = null,
    val id: Long,
    val image: Image,
    @JsonNames("count_of_issues")
    val issueCount: Int,
    val lastIssue: IssueEntry? = null,
    val name: String,
    val publisher: GenericEntry? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    @Serializable(with = StartYearSerializer::class)
    val startYear: Int? = null,
    @JsonNames("deck")
    val summary: String? = null,
) : Comparable<VolumeEntry> {
    companion object {
        private val comparator = compareBy(VolumeEntry::name).thenBy { it.startYear }
    }

    override fun compareTo(other: VolumeEntry): Int = comparator.compare(this, other)
}
