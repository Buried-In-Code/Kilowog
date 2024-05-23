package github.comiccorps.metron.schemas

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Team(
    @JsonNames("cv_id")
    val comicvineId: Long? = null,
    val creators: List<BaseResource> = emptyList(),
    @JsonNames("desc")
    val description: String? = null,
    val id: Long,
    val image: String? = null,
    val modified: Instant,
    val name: String,
    val resourceUrl: String,
    val universes: List<BaseResource> = emptyList(),
)
