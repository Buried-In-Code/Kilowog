package github.comiccorps.kilowog.models.metroninfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Genre {
    @SerialName("Adult")
    ADULT,

    @SerialName("Crime")
    CRIME,

    @SerialName("Espionage")
    ESPIONAGE,

    @SerialName("Fantasy")
    FANTASY,

    @SerialName("Historical")
    HISTORICAL,

    @SerialName("Horror")
    HORROR,

    @SerialName("Humor")
    HUMOR,

    @SerialName("Manga")
    MANGA,

    @SerialName("Parody")
    PARODY,

    @SerialName("Romance")
    ROMANCE,

    @SerialName("Science Fiction")
    SCIENCE_FICTION,

    @SerialName("Sport")
    SPORT,

    @SerialName("Super-Hero")
    SUPER_HERO,

    @SerialName("War")
    WAR,

    @SerialName("Western")
    WESTERN,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
