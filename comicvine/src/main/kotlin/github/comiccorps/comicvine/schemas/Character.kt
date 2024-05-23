package github.comiccorps.comicvine.schemas

import github.comiccorps.comicvine.LocalDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BasicCharacter(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateAdded: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateLastUpdated: LocalDateTime,
    val birth: LocalDate? = null,
    val description: String? = null,
    @JsonNames("first_appeared_in_issue")
    val firstIssue: GenericIssue? = null,
    val gender: Int,
    val id: Long,
    val image: Image,
    @JsonNames("count_of_issue_appearances")
    val issueCount: Int,
    val name: String,
    val origin: GenericEntry? = null,
    val publisher: GenericEntry? = null,
    val realName: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    @JsonNames("deck")
    val summary: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Character(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateAdded: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateLastUpdated: LocalDateTime,
    val birth: LocalDate? = null,
    val description: String? = null,
    @JsonNames("first_appeared_in_issue")
    val firstIssue: GenericIssue? = null,
    val gender: Int,
    val id: Long,
    val image: Image,
    @JsonNames("count_of_issue_appearances")
    val issueCount: Int,
    val name: String,
    val origin: GenericEntry? = null,
    val publisher: GenericEntry? = null,
    val realName: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    @JsonNames("deck")
    val summary: String? = null,
    val creators: List<GenericEntry> = emptyList(),
    @JsonNames("issues_died_in")
    val deaths: List<GenericEntry> = emptyList(),
    @JsonNames("character_enemies")
    val enemies: List<GenericEntry> = emptyList(),
    @JsonNames("team_enemies")
    val enemyTeams: List<GenericEntry> = emptyList(),
    @JsonNames("team_friends")
    val friendlyTeams: List<GenericEntry> = emptyList(),
    @JsonNames("character_friends")
    val friends: List<GenericEntry> = emptyList(),
    @JsonNames("issue_credits")
    val issues: List<GenericEntry> = emptyList(),
    val powers: List<GenericEntry> = emptyList(),
    @JsonNames("story_arc_credits")
    val storyArcs: List<GenericEntry> = emptyList(),
    val teams: List<GenericEntry> = emptyList(),
    @JsonNames("volume_credits")
    val volumes: List<GenericEntry> = emptyList(),
)
