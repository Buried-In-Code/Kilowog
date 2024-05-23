package github.comiccorps.kilowog.models.metadata

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Format {
    @SerialName("Annual")
    ANNUAL,

    @SerialName("Comic")
    COMIC,

    @SerialName("Digital Chapter")
    DIGITAL_CHAPTER,

    @SerialName("Graphic Novel")
    GRAPHIC_NOVEL,

    @SerialName("Hardcover")
    HARDCOVER,

    @SerialName("Trade Paperback")
    TRADE_PAPERBACK,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
