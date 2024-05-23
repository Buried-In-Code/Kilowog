package github.comiccorps.kilowog.models.comicinfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class YesNo {
    @SerialName("Yes")
    YES,

    @SerialName("No")
    NO,

    @SerialName("Unknown")
    UNKNOWN,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
