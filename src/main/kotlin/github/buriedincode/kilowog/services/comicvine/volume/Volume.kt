package github.buriedincode.kilowog.services.comicvine.volume

import github.buriedincode.kilowog.LocalDateTimeSerializer
import github.buriedincode.kilowog.services.comicvine.CountEntry
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image
import github.buriedincode.kilowog.services.comicvine.IssueEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Volume(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateAdded: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateLastUpdated: LocalDateTime,
    val description: String? = null,
    val firstIssue: IssueEntry? = null,
    val image: Image,
    @JsonNames("count_of_issues")
    val issueCount: Int,
    val lastIssue: IssueEntry? = null,
    val name: String,
    val publisher: GenericEntry? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    val startYear: Int? = null,
    @JsonNames("deck")
    val summary: String? = null,
    @JsonNames("id")
    val volumeId: Int,
    val characters: List<CountEntry> = emptyList(),
    val concepts: List<CountEntry> = emptyList(),
    @JsonNames("people")
    val creators: List<CountEntry> = emptyList(),
    val issues: List<IssueEntry> = emptyList(),
    val locations: List<CountEntry> = emptyList(),
    val objects: List<CountEntry> = emptyList(),
)
