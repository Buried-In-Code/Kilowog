package github.buriedincode.metron.schemas

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BasicSeries(
    val id: Long,
    val issueCount: Int,
    val modified: Instant,
    @JsonNames("series")
    val name: String,
    val volume: Int,
    val yearBegan: Int,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Series(
    val associated: List<Associated> = emptyList(),
    @JsonNames("cv_id")
    val comicvineId: Long? = null,
    @JsonNames("desc")
    val description: String? = null,
    val genres: List<GenericItem> = emptyList(),
    val id: Long,
    val issueCount: Int,
    val modified: Instant,
    val name: String,
    val publisher: GenericItem,
    val resourceUrl: String,
    val seriesType: GenericItem,
    val sortName: String,
    val volume: Int,
    val yearBegan: Int,
    val yearEnd: Int? = null,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Associated(
        val id: Long,
        @JsonNames("series")
        val name: String,
    )
}
