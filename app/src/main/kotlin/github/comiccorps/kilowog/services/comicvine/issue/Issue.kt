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
data class Issue(
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
    @JsonNames("character_credits")
    val characters: List<GenericEntry> = emptyList(),
    @JsonNames("concept_credits")
    val concepts: List<GenericEntry> = emptyList(),
    @JsonNames("person_credits")
    val creators: List<CreatorEntry> = emptyList(),
    @JsonNames("character_died_in")
    val deaths: List<GenericEntry> = emptyList(),
    val firstAppearanceCharacters: List<GenericEntry>? = emptyList(),
    val firstAppearanceConcepts: List<GenericEntry>? = emptyList(),
    val firstAppearanceLocations: List<GenericEntry>? = emptyList(),
    val firstAppearanceObjects: List<GenericEntry>? = emptyList(),
    @JsonNames("first_appearance_storyarcs")
    val firstAppearanceStoryArcs: List<GenericEntry>? = emptyList(),
    val firstAppearanceTeams: List<GenericEntry>? = emptyList(),
    @JsonNames("location_credits")
    val locations: List<GenericEntry> = emptyList(),
    @JsonNames("object_credits")
    val objects: List<GenericEntry> = emptyList(),
    @JsonNames("story_arc_credits")
    val storyArcs: List<GenericEntry> = emptyList(),
    @JsonNames("team_credits")
    val teams: List<GenericEntry> = emptyList(),
    @JsonNames("team_disbanded_in")
    val teamsDisbanded: List<GenericEntry> = emptyList(),
) {
    @Serializable
    data class CreatorEntry(
        @JsonNames("api_detail_url")
        val apiUrl: String,
        val id: Long,
        val name: String? = null,
        @JsonNames("site_detail_url")
        val siteUrl: String? = null,
        @JsonNames("role")
        val roles: String,
    )
}
