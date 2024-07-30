package github.buriedincode.kilowog

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.addResourceSource
import java.nio.file.Path
import kotlin.io.path.div

data class Settings(
    val inputFolder: Path,
    val outputFolder: Path,
    val comicvine: Comicvine? = null,
    val leagueOfComicGeeks: LeagueOfComicGeeks? = null,
    val marvel: Marvel? = null,
    val metron: Metron? = null,
    val serviceOrder: List<Service>,
    val output: Output,
) {
    data class Comicvine(val apiKey: Secret? = null)

    data class LeagueOfComicGeeks(val clientId: String? = null, val clientSecret: Secret? = null, val accessToken: Secret? = null)

    data class Marvel(val publicKey: String? = null, val privateKey: Secret? = null)

    data class Metron(val username: String? = null, val password: Secret? = null)

    data class Output(val createComicInfo: Boolean, val createMetronInfo: Boolean, val createMetadata: Boolean, val format: Format) {
        enum class Format {
            CB7,
            CBT,
            CBZ,
        }
    }

    enum class Service {
        COMICVINE,
        LEAGUE_OF_COMIC_GEEKS,
        MARVEL,
        METRON,
    }

    companion object {
        fun load(): Settings = ConfigLoaderBuilder
            .default()
            .addPathSource(Utils.CONFIG_ROOT / "settings.properties", optional = true, allowEmpty = true)
            .addResourceSource("/default.properties")
            .build()
            .loadConfigOrThrow<Settings>()
    }
}
