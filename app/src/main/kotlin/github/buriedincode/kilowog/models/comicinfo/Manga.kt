package github.buriedincode.kilowog.models.comicinfo

import github.buriedincode.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Manga {
    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("No")
    NO,

    @SerialName("Yes")
    YES,

    @SerialName("YesAndRightToLeft")
    YES_AND_RIGHT_TO_LEFT,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
