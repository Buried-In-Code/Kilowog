package github.comiccorps.comicvine.schemas

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class GenericEntry(
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val id: Int,
    val name: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class GenericIssue(
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val id: Int,
    val name: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String? = null,
    @JsonNames("issue_number")
    val number: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Image(
    val iconUrl: String,
    val mediumUrl: String,
    val originalUrl: String,
    val screenLargeUrl: String,
    val screenUrl: String,
    val smallUrl: String,
    val superUrl: String,
    val thumbUrl: String,
    val tinyUrl: String,
    @JsonNames("image_tags")
    val tags: String? = null,
)
