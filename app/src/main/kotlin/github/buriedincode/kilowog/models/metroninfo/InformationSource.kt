package github.buriedincode.kilowog.models.metroninfo

import github.buriedincode.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class InformationSource {
    @SerialName("Comic Vine")
    COMIC_VINE,

    @SerialName("Grand Comics Database")
    GRAND_COMICS_DATABASE,

    @SerialName("Marvel")
    MARVEL,

    @SerialName("Metron")
    METRON,

    @SerialName("League of Comic Geeks")
    LEAGUE_OF_COMIC_GEEKS,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
