package github.comiccorps.kilowog.services

import com.sksamuel.hoplite.Secret
import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.services.comicvine.Response
import github.comiccorps.kilowog.services.comicvine.issue.Issue
import github.comiccorps.kilowog.services.comicvine.issue.IssueEntry
import github.comiccorps.kilowog.services.comicvine.publisher.Publisher
import github.comiccorps.kilowog.services.comicvine.publisher.PublisherEntry
import github.comiccorps.kilowog.services.comicvine.volume.Volume
import github.comiccorps.kilowog.services.comicvine.volume.VolumeEntry
import kotlinx.serialization.SerializationException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.stream.Collectors

data class Comicvine(private val apiKey: String, private val cache: SQLiteCache? = null) {
    constructor(apiKey: Secret, cache: SQLiteCache? = null) : this(apiKey = apiKey.value, cache = cache)

    private val regex = "api_key=(.+?)&".toRegex()

    private fun encodeURI(endpoint: String, params: MutableMap<String, String> = mutableMapOf()): URI {
        params["api_key"] = apiKey
        params["format"] = "json"
        val encodedUrl = params.keys
            .stream()
            .sorted()
            .map {
                "$it=${URLEncoder.encode(params[it], StandardCharsets.UTF_8)}"
            }
            .collect(Collectors.joining("&", "$BASE_API$endpoint?", ""))
        return URI.create(encodedUrl)
    }

    private fun sendRequest(uri: URI): String? {
        val adjustedUri = regex.replaceFirst(uri.toString(), "api_key=***&")
        if (this.cache != null) {
            val cachedResponse = cache.select(url = adjustedUri)
            if (cachedResponse != null) {
                logger.debug("Using cached response for $adjustedUri")
                return cachedResponse
            }
        }
        try {
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Accept", "application/json")
                .setHeader(
                    "User-Agent",
                    "Kilowog-v${Utils.VERSION}/Java-v${System.getProperty("java.version")}",
                )
                .GET()
                .build()
            val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            val level = when {
                response.statusCode() in (100 until 200) -> Level.WARN
                response.statusCode() in (200 until 300) -> Level.DEBUG
                response.statusCode() in (300 until 400) -> Level.INFO
                response.statusCode() in (400 until 500) -> Level.WARN
                else -> Level.ERROR
            }
            logger.log(level, "GET: ${response.statusCode()} - $adjustedUri")
            if (response.statusCode() == 200) {
                return response.body()
            }
            logger.error(response.body())
        } catch (exc: IOException) {
            logger.error("Unable to make request to: $adjustedUri", exc)
        } catch (exc: InterruptedException) {
            logger.error("Unable to make request to: $adjustedUri", exc)
        }
        return null
    }

    fun listPublishers(params: Map<String, String> = emptyMap()): List<PublisherEntry> {
        val temp = params.toMutableMap()
        temp["page"] = temp.getOrDefault("page", 1).toString()
        temp["limit"] = temp.getOrDefault("limit", PAGE_LIMIT).toString()
        temp["offset"] = temp.getOrDefault("offset", temp["page"]!!.toInt() * PAGE_LIMIT).toString()
        val uri = encodeURI(endpoint = "/publishers", params = temp)
        try {
            val content: String = sendRequest(uri = uri) ?: return emptyList()
            val response: Response<ArrayList<PublisherEntry>> = Utils.JSON_MAPPER.decodeFromString(content)
            val results = response.results
            if (results.isNotEmpty() && this.cache != null) {
                cache.insert(url = regex.replaceFirst(uri.toString(), "api_key=***&"), response = content)
            }
            if (response.totalResults >= temp["page"]!!.toInt() * PAGE_LIMIT) {
                temp["page"] = (temp["page"]!!.toInt() + 1).toString()
                results.addAll(this.listPublishers(params = temp))
            }
            return results
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return emptyList()
        }
    }

    fun getPublisher(publisherId: Long): Publisher? {
        val uri = encodeURI(endpoint = "/publisher/${Resource.PUBLISHER.resourceId}-$publisherId")
        val content = sendRequest(uri = uri)
        if (content != null && this.cache != null) {
            cache.insert(url = regex.replaceFirst(uri.toString(), "api_key=***&"), response = content)
        }
        val response: Response<Publisher>? = try {
            Utils.JSON_MAPPER.decodeFromString<Response<Publisher>>(content ?: "Invalid")
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            logger.debug(content ?: "")
            null
        }
        return response?.results
    }

    fun listVolumes(params: Map<String, String> = emptyMap()): List<VolumeEntry> {
        val temp = params.toMutableMap()
        temp["page"] = temp.getOrDefault("page", 1).toString()
        temp["limit"] = temp.getOrDefault("limit", PAGE_LIMIT).toString()
        temp["offset"] = temp.getOrDefault("offset", temp["page"]!!.toInt() * PAGE_LIMIT).toString()
        val uri = encodeURI(endpoint = "/volumes", params = temp)
        try {
            val content: String = sendRequest(uri = uri) ?: return emptyList()
            val response: Response<ArrayList<VolumeEntry>> = Utils.JSON_MAPPER.decodeFromString(content)
            val results = response.results
            if (results.isNotEmpty() && this.cache != null) {
                cache.insert(url = regex.replaceFirst(uri.toString(), "api_key=***&"), response = content)
            }
            if (response.totalResults >= temp["page"]!!.toInt() * PAGE_LIMIT) {
                temp["page"] = (temp["page"]!!.toInt() + 1).toString()
                results.addAll(this.listVolumes(params = temp))
            }
            return results
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return emptyList()
        }
    }

    fun getVolume(volumeId: Long): Volume? {
        val uri = encodeURI(endpoint = "/volume/${Resource.VOLUME.resourceId}-$volumeId")
        val content = sendRequest(uri = uri)
        if (content != null && this.cache != null) {
            cache.insert(url = regex.replaceFirst(uri.toString(), "api_key=***&"), response = content)
        }
        val response: Response<Volume>? = try {
            Utils.JSON_MAPPER.decodeFromString<Response<Volume>>(content ?: "Invalid")
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            logger.debug(content ?: "")
            null
        }
        return response?.results
    }

    fun listIssues(params: Map<String, String> = emptyMap()): List<IssueEntry> {
        val temp = params.toMutableMap()
        temp["page"] = temp.getOrDefault("page", 1).toString()
        temp["limit"] = temp.getOrDefault("limit", PAGE_LIMIT).toString()
        temp["offset"] = temp.getOrDefault("offset", temp["page"]!!.toInt() * PAGE_LIMIT).toString()
        val uri = encodeURI(endpoint = "/issues", params = temp)
        try {
            val content: String = sendRequest(uri = uri) ?: return emptyList()
            val response: Response<ArrayList<IssueEntry>> = Utils.JSON_MAPPER.decodeFromString(content)
            val results = response.results
            if (results.isNotEmpty() && this.cache != null) {
                cache.insert(url = regex.replaceFirst(uri.toString(), "api_key=***&"), response = content)
            }
            if (response.totalResults >= temp["page"]!!.toInt() * PAGE_LIMIT) {
                temp["page"] = (temp["page"]!!.toInt() + 1).toString()
                results.addAll(this.listIssues(params = temp))
            }
            return results
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return emptyList()
        }
    }

    fun getIssue(issueId: Long): Issue? {
        val uri = encodeURI(endpoint = "/issue/${Resource.ISSUE.resourceId}-$issueId")
        val content = sendRequest(uri = uri)
        if (content != null && this.cache != null) {
            cache.insert(url = regex.replaceFirst(uri.toString(), "api_key=***&"), response = content)
        }
        val response: Response<Issue>? = try {
            Utils.JSON_MAPPER.decodeFromString<Response<Issue>>(content ?: "Invalid")
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            logger.debug(content ?: "")
            null
        }
        return response?.results
    }

    private enum class Resource(val resourceId: Int) {
        PUBLISHER(4010),
        VOLUME(4050),
        ISSUE(4000),
    }

    companion object : Logging {
        private const val BASE_API = "https://comicvine.gamespot.com/api"
        private val CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(5))
            .build()
        private const val PAGE_LIMIT = 100
    }
}
