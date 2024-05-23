package github.comiccorps.kilowog.models.metroninfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class InformationSource {
    @SerialName("Comic Vine")
    COMIC_VINE,

    @SerialName("Grand Comics Database")
    GRAND_COMICS_DATABASE,

    @SerialName("League of Comic Geeks")
    LEAGUE_OF_COMIC_GEEKS,

    @SerialName("Marvel")
    MARVEL,

    @SerialName("Metron")
    METRON,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
