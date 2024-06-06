package github.comiccorps.kilowog.services.comicvine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Response<T>(
    val error: String,
    val limit: Int,
    val offset: Int,
    @JsonNames("number_of_page_results")
    val pageResults: Int,
    val results: T,
    val statusCode: Int,
    @JsonNames("number_of_total_results")
    val totalResults: Int,
    val version: String,
)
