package github.comiccorps.metron.schemas

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Creator(
    val alias: List<String> = emptyList(),
    val birth: LocalDate? = null,
    @JsonNames("cv_id")
    val comicvineId: Long? = null,
    val death: LocalDate? = null,
    @JsonNames("desc")
    val description: String? = null,
    val id: Long,
    val image: String? = null,
    val modified: Instant,
    val name: String,
    val resourceUrl: String,
)
