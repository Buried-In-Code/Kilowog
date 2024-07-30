package github.buriedincode.metron.schemas

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Arc(
    @JsonNames("cv_id")
    val comicvineId: Long? = null,
    @JsonNames("desc")
    val description: String? = null,
    val id: Long,
    val image: String? = null,
    val modified: Instant,
    val name: String,
    val resourceUrl: String,
)
