package github.comiccorps.kilowog.models.comicinfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class AgeRating {
    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("Adults Only 18+")
    ADULTS_ONLY,

    @SerialName("Early Childhood")
    EARLY_CHILDHOOD,

    @SerialName("Everyone")
    EVERYONE,

    @SerialName("Everyone 10+")
    EVERONE_10,

    @SerialName("G")
    G,

    @SerialName("Kids to Adults")
    KIDS_TO_ADULTS,

    @SerialName("M")
    M,

    @SerialName("MA15+")
    MA15,

    @SerialName("Mature 17+")
    MATURE_17,

    @SerialName("PG")
    PG,

    @SerialName("R18+")
    R18,

    @SerialName("Rating Pending")
    RATING_PENDING,

    @SerialName("Teen")
    TEEN,

    @SerialName("X18+")
    X18,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
