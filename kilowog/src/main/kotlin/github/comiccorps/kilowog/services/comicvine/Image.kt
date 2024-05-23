package github.comiccorps.kilowog.services.comicvine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Image(
    val iconUrl: String,
    val mediumUrl: String,
    val originalUrl: String,
    val screenUrl: String,
    val screenLargeUrl: String,
    val smallUrl: String,
    val superUrl: String,
    @JsonNames("thumb_url")
    val thumbnail: String,
    val tinyUrl: String,
    @JsonNames("image_tags")
    val tags: String? = null,
)
