package github.comiccorps.comicvine

import github.comiccorps.comicvine.schemas.BasicCharacter
import github.comiccorps.comicvine.schemas.Character
import github.comiccorps.comicvine.schemas.GenericEntry
import github.comiccorps.comicvine.schemas.Response
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import kotlin.collections.joinToString
import kotlin.collections.plus
import kotlin.collections.sortedBy
import kotlin.jvm.Throws
import kotlin.let
import kotlin.ranges.until
import kotlin.text.isNotEmpty

class Comicvine(
    private val apiKey: String,
    private val cache: SQLiteCache? = null,
    timeout: Duration = Duration.ofSeconds(30),
) {
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .connectTimeout(timeout)
        .build()

    fun encodeURI(endpoint: String, params: Map<String, String> = emptyMap()): URI {
        val temp = params.toMutableMap().apply {
            put("api_key", this@Comicvine.apiKey)
            put("format", "json")
        }
        val encodedParams = temp.entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}" }
        return URI.create("$BASE_API$endpoint${if (encodedParams.isNotEmpty()) "?$encodedParams" else ""}")
    }

    @Throws(ServiceException::class, AuthenticationException::class)
    private fun performGetRequest(uri: URI): String {
        try {
            @Suppress("ktlint")
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Accept", "application/json")
                .setHeader("User-Agent", "Kilowog-Comicvine/0.2.0 (${System.getProperty("os.name")}/${System.getProperty("os.version")}; Kotlin/${KotlinVersion.CURRENT})")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val level = when (response.statusCode()) {
                in 100 until 200 -> Level.WARN
                in 200 until 300 -> Level.DEBUG
                in 300 until 400 -> Level.INFO
                in 400 until 500 -> Level.WARN
                else -> Level.ERROR
            }
            logger.log(level, "GET: ${response.statusCode()} - $uri")
            if (response.statusCode() == 200) {
                return response.body()
            }

            val content = JSON.parseToJsonElement(response.body()).jsonObject
            logger.error(content.toString())
            throw when (response.statusCode()) {
                401 -> AuthenticationException(content["detail"]?.jsonPrimitive?.content ?: "")
                404 -> ServiceException("Resource not found")
                else -> ServiceException(content["detail"]?.jsonPrimitive?.content ?: "")
            }
        } catch (ioe: IOException) {
            throw ServiceException(cause = ioe)
        } catch (hcte: HttpConnectTimeoutException) {
            throw ServiceException(cause = hcte)
        } catch (ie: InterruptedException) {
            throw ServiceException(cause = ie)
        } catch (se: SerializationException) {
            throw ServiceException(cause = se)
        }
    }

    @Throws(ServiceException::class, AuthenticationException::class)
    internal inline fun <reified T> getRequest(uri: URI): Response<T> {
        val cacheKey = API_KEY_REGEX.replaceFirst(uri.toString(), "api_key=***&")
        this.cache?.select(url = cacheKey)?.let {
            try {
                logger.debug("Using cached response for $cacheKey")
                return JSON.decodeFromString(it)
            } catch (se: SerializationException) {
                logger.warn("Unable to deserialize cached response", se)
                this.cache.delete(url = cacheKey)
            }
        }
        val response = this.performGetRequest(uri = uri)
        this.cache?.insert(url = cacheKey, response = response)
        return try {
            JSON.decodeFromString(response)
        } catch (se: SerializationException) {
            throw ServiceException(cause = se)
        }
    }

    @Throws(ServiceException::class, AuthenticationException::class)
    private inline fun <reified T> fetchList(endpoint: String, params: Map<String, String>): List<T> {
        val resultList = mutableListOf<T>()
        var page = params.getOrDefault("page", "1").toInt()
        val limit = params.getOrDefault("limit", "100").toInt()
        val offset = params.getOrDefault("offset", "${(page - 1) * limit}").toInt()

        do {
            val uri = this.encodeURI(
                endpoint = endpoint,
                params = params + ("page" to page.toString()) + ("limit" to limit.toString()) + ("offset" to offset.toString()),
            )
            val response = this.getRequest<List<T>>(uri = uri)
            resultList.addAll(response.results)
            page++
        } while (response.totalResults >= page * limit)

        return resultList
    }

    @Throws(ServiceException::class, AuthenticationException::class)
    private inline fun <reified T> fetchItem(endpoint: String): T = this.getRequest<T>(uri = this.encodeURI(endpoint = endpoint)).results

    @Throws(ServiceException::class, AuthenticationException::class)
    fun listCharacters(params: Map<String, String> = emptyMap()): List<BasicCharacter> {
        return this.fetchList(endpoint = "/characters", params = params)
    }

    @Throws(ServiceException::class, AuthenticationException::class)
    fun getCharacter(id: Long): Character = this.fetchItem(endpoint = "/character/4005-$id")

    @Throws(ServiceException::class, AuthenticationException::class)
    fun listPublishers(params: Map<String, String> = emptyMap()): List<GenericEntry> {
        return this.fetchList(endpoint = "/publishers", params = params)
    }

    @Throws(ServiceException::class, AuthenticationException::class)
    fun getPublisher(id: Long): GenericEntry = this.fetchItem(endpoint = "/publisher/4010-$id")

    companion object : Logging {
        private const val BASE_API = "https://comicvine.gamespot.com/api"
        private val API_KEY_REGEX = "api_key=(.+?)&".toRegex()

        @OptIn(ExperimentalSerializationApi::class)
        private val JSON: Json = Json {
            prettyPrint = true
            encodeDefaults = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }
}
