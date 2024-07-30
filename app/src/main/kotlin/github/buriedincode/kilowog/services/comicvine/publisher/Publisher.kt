package github.buriedincode.kilowog.services.comicvine.publisher

import github.buriedincode.kilowog.LocalDateTimeSerializer
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Publisher(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateAdded: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateLastUpdated: LocalDateTime,
    val description: String? = null,
    val id: Long,
    val image: Image,
    val locationAddress: String? = null,
    val locationCity: String? = null,
    val locationState: String? = null,
    val name: String,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    @JsonNames("deck")
    val summary: String? = null,
    val characters: List<GenericEntry> = emptyList(),
    val storyArcs: List<GenericEntry> = emptyList(),
    val teams: List<GenericEntry> = emptyList(),
    val volumes: List<GenericEntry> = emptyList(),
)
