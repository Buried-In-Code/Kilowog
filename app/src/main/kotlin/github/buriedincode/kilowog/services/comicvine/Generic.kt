package github.buriedincode.kilowog.services.comicvine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class GenericEntry(
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val id: Long,
    val name: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssueEntry(
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val id: Long,
    val name: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String? = null,
    @JsonNames("issue_number")
    val number: String? = null,
)
