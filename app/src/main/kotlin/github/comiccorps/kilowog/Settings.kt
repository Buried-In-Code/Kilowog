package github.comiccorps.kilowog

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.addResourceSource
import java.nio.file.Path
import kotlin.io.path.div

data class Settings(
    val collectionFolder: Path,
    val comicvine: Comicvine? = null,
    val leagueOfComicGeeks: LeagueOfComicGeeks? = null,
    val marvel: Marvel? = null,
    val metron: Metron? = null,
    val output: Output,
) {
    data class Comicvine(val apiKey: Secret? = null)

    data class LeagueOfComicGeeks(val accessToken: Secret? = null, val clientId: String? = null, val clientSecret: Secret? = null)

    data class Marvel(val publicKey: String? = null, val privateKey: Secret? = null)

    data class Metron(val username: String? = null, val password: Secret? = null)

    data class Output(val createComicInfo: Boolean, val createMetadata: Boolean, val createMetronInfo: Boolean, val format: Format) {
        enum class Format {
            CB7,
            CBT,
            CBZ,
        }
    }

    companion object {
        fun load(): Settings =
            ConfigLoaderBuilder.default()
                .addPathSource(Utils.CONFIG_ROOT / "settings.properties", optional = true, allowEmpty = true)
                .addResourceSource("/default.properties")
                .build()
                .loadConfigOrThrow<Settings>()
    }
}
