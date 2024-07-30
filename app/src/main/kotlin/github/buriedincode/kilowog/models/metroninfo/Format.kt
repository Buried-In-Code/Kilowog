package github.buriedincode.kilowog.models.metroninfo

import github.buriedincode.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Format {
    @SerialName("Annual")
    ANNUAL,

    @SerialName("Digital Chapters")
    DIGITAL_CHAPTERS,

    @SerialName("Graphic Novel")
    GRAPHIC_NOVEL,

    @SerialName("Hardcover")
    HARDCOVER,

    @SerialName("Limited Series")
    LIMITED_SERIES,

    @SerialName("Omnibus")
    OMNIBUS,

    @SerialName("One-Shot")
    ONE_SHOT,

    @SerialName("Single Issue")
    SINGLE_ISSUE,

    @SerialName("Trade Paperback")
    TRADE_PAPERBACK,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
