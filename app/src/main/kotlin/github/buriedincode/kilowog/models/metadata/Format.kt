package github.buriedincode.kilowog.models.metadata

import github.buriedincode.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Format {
    @SerialName("Annual")
    ANNUAL,

    @SerialName("Digital Chapter")
    DIGITAL_CHAPTER,

    @SerialName("Graphic Novel")
    GRAPHIC_NOVEL,

    @SerialName("Hardcover")
    HARDCOVER,

    @SerialName("Omnibus")
    OMNIBUS,

    @SerialName("Single Issue")
    SINGLE_ISSUE,

    @SerialName("Trade Paperback")
    TRADE_PAPERBACK,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
