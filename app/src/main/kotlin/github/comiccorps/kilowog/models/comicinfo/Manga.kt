package github.comiccorps.kilowog.models.comicinfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Manga {
    @SerialName("Yes")
    YES,

    @SerialName("No")
    NO,

    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("YesAndRightToLeft")
    YES_AND_RIGHT_TO_LEFT,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
