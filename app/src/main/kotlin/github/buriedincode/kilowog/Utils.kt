package github.buriedincode.kilowog

import com.sksamuel.hoplite.Secret
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.XmlUtilInternal
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div
import kotlin.io.walkTopDown

object Utils {
    private val HOME_ROOT: Path = Paths.get(System.getProperty("user.home"))
    private val CACHE_HOME: Path = System.getenv("XDG_CACHE_HOME")?.let {
        Paths.get(it)
    } ?: (HOME_ROOT / ".cache")
    private val CONFIG_HOME: Path = System.getenv("XDG_CONFIG_HOME")?.let {
        Paths.get(it)
    } ?: (HOME_ROOT / ".config")
    private val DATA_HOME: Path = System.getenv("XDG_DATA_HOME")?.let {
        Paths.get(it)
    } ?: (HOME_ROOT / ".local" / "share")

    internal val CACHE_ROOT = CACHE_HOME / "kilowog"
    internal val CONFIG_ROOT = CONFIG_HOME / "kilowog"
    internal val DATA_ROOT = DATA_HOME / "kilowog"
    internal const val VERSION = "0.2.1"

    internal val archiveExtensions: Array<String> = arrayOf("cb7", "cbr", "cbt", "cbz")
    internal val imageExtensions: Array<String> = arrayOf("jpg", "jpeg", "png", "webp")

    @OptIn(XmlUtilInternal::class)
    val XML_MAPPER: XML = XML {
        indent = 4
        xmlDeclMode = XmlDeclMode.Charset
        xmlVersion = XmlVersion.XML10
        repairNamespaces = true
        jacksonPolicy()
    }

    @OptIn(ExperimentalSerializationApi::class)
    val JSON_MAPPER: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    init {
        if (!Files.exists(CACHE_ROOT)) {
            CACHE_ROOT.toFile().mkdirs()
        }
        if (!Files.exists(CONFIG_ROOT)) {
            CONFIG_ROOT.toFile().mkdirs()
        }
        if (!Files.exists(DATA_ROOT)) {
            DATA_ROOT.toFile().mkdirs()
        }
    }

    internal fun listFiles(path: Path, vararg extensions: String): List<Path> {
        require(Files.isDirectory(path)) { "'$path' must be a directory" }

        return path
            .toFile()
            .walkTopDown()
            .onEnter { !it.name.startsWith(".") }
            .filter { file ->
                file.isFile &&
                    (
                        extensions.isEmpty() ||
                            extensions.any { extension ->
                                file.extension.equals(extension, ignoreCase = true)
                            }
                    )
            }.map { it.toPath() }
            .toList()
    }

    internal fun Secret?.isNullOrBlank(): Boolean = this?.value.isNullOrBlank()

    inline fun <reified T : Enum<T>> String.asEnumOrNull(): T? {
        return enumValues<T>().firstOrNull {
            it.name.replace("_", " ").equals(this.replace("_", " "), ignoreCase = true)
        }
    }

    inline fun <reified T : Enum<T>> T.titlecase(): String {
        return this.name.lowercase().split("_").joinToString(" ") {
            it.replaceFirstChar(Char::uppercaseChar)
        }
    }

    fun <T, K, V> Iterable<T>.associateNotNull(transform: (T) -> Pair<K?, V?>): Map<K, V> {
        val resultMap = mutableMapOf<K, V>()
        for (item in this) {
            val pair = transform(item)
            if (pair.first == null || pair.second == null) {
                continue
            }
            resultMap[pair.first!!] = pair.second!!
        }
        return resultMap
    }

    fun sanitize(value: String): String {
        var output: String = value.replace("[\\\\/:*?\"<>|]+".toRegex(), "")
        output = output.replace("-", " ")
        output = output.split(" ").filterNot { it.isBlank() }.joinToString(" ") { it.trim() }
        return output.replace(" ", "-")
    }
}
