package github.comiccorps.kilowog.models.metroninfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class AgeRating {
    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("Everyone")
    EVERYONE,

    @SerialName("Teen")
    TEEN,

    @SerialName("Teen Plus")
    TEEN_PLUS,

    @SerialName("Mature")
    MATURE,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
