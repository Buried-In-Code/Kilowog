package github.buriedincode.kilowog.models.comicinfo

import github.buriedincode.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class YesNo {
    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("No")
    NO,

    @SerialName("Yes")
    YES,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
