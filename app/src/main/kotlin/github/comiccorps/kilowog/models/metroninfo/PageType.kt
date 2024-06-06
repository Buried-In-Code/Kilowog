package github.comiccorps.kilowog.models.metroninfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class PageType {
    @SerialName("FrontCover")
    FRONT_COVER,

    @SerialName("InnerCover")
    INNER_COVER,

    @SerialName("Roundup")
    ROUNDUP,

    @SerialName("Story")
    STORY,

    @SerialName("Advertisement")
    ADVERTISEMENT,

    @SerialName("Editorial")
    EDITORIAL,

    @SerialName("Letters")
    LETTERS,

    @SerialName("Preview")
    PREVIEW,

    @SerialName("BackCover")
    BACK_COVER,

    @SerialName("Other")
    OTHER,

    @SerialName("Deleted")
    DELETED,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
