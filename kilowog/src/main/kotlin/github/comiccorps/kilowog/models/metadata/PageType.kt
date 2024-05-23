package github.comiccorps.kilowog.models.metadata

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class PageType {
    @SerialName("Advertisement")
    ADVERTISEMENT,

    @SerialName("Back Cover")
    BACK_COVER,

    @SerialName("Editorial")
    EDITORIAL,

    @SerialName("Front Cover")
    FRONT_COVER,

    @SerialName("Inner Cover")
    INNER_COVER,

    @SerialName("Letters")
    LETTERS,

    @SerialName("Other")
    OTHER,

    @SerialName("Preview")
    PREVIEW,

    @SerialName("Roundup")
    ROUNDUP,

    @SerialName("Story")
    STORY,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
