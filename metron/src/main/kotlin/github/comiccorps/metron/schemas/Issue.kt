package github.comiccorps.metron.schemas

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BasicIssue(
    val coverDate: LocalDate,
    val coverHash: String? = null,
    val id: Long,
    val image: String? = null,
    val modified: Instant,
    @JsonNames("issue")
    val name: String,
    val number: String,
    val series: Series,
    val storeDate: LocalDate? = null,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Series(
        val name: String,
        val volume: Int,
        val yearBegan: Int,
    )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Issue(
    val arcs: List<BaseResource> = emptyList(),
    val characters: List<BaseResource> = emptyList(),
    @JsonNames("cv_id")
    val comicvineId: Long? = null,
    val coverDate: LocalDate,
    val coverHash: String? = null,
    val credits: List<Credit> = emptyList(),
    @JsonNames("desc")
    val description: String? = null,
    val id: Long,
    val image: String? = null,
    val isbn: String? = null,
    val modified: Instant,
    val number: String,
    @JsonNames("page")
    val pageCount: Int? = null,
    val price: Double? = null,
    val publisher: GenericItem,
    val rating: GenericItem,
    val reprints: List<Reprint> = emptyList(),
    val resourceUrl: String,
    val series: Series,
    val sku: String? = null,
    val storeDate: LocalDate? = null,
    @JsonNames("name")
    val stories: List<String> = emptyList(),
    val teams: List<BaseResource> = emptyList(),
    val title: String? = null,
    val universes: List<BaseResource> = emptyList(),
    val upc: String? = null,
    val variants: List<Variant> = emptyList(),
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Credit(
        val creator: String,
        val id: Long,
        val role: List<GenericItem> = emptyList(),
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Reprint(
        val id: Long,
        @JsonNames("issue")
        val name: String,
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Series(
        val genres: List<GenericItem> = emptyList(),
        val id: Long,
        val name: String,
        val seriesType: GenericItem,
        val sortName: String,
        val volume: Int,
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Variant(
        val image: String,
        val name: String? = null,
        val sku: String? = null,
        val upc: String? = null,
    )
}
