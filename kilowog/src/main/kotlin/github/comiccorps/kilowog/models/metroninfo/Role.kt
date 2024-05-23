package github.comiccorps.kilowog.models.metroninfo

import github.comiccorps.kilowog.Utils.titlecase
import kotlinx.serialization.SerialName

enum class Role {
    @SerialName("Writer")
    WRITER,

    @SerialName("Script")
    SCRIPT,

    @SerialName("Story")
    STORY,

    @SerialName("Plot")
    PLOT,

    @SerialName("Interviewer")
    INTERVIEWER,

    @SerialName("Artist")
    ARTIST,

    @SerialName("Penciller")
    PENCILLER,

    @SerialName("Breakdowns")
    BREAKDOWNS,

    @SerialName("Illustrator")
    ILLUSTRATOR,

    @SerialName("Layouts")
    LAYOUTS,

    @SerialName("Inker")
    INKER,

    @SerialName("Embellisher")
    EMBELLISHER,

    @SerialName("Finishes")
    FINISHES,

    @SerialName("Ink Assists")
    INK_ASSISTS,

    @SerialName("Colorist")
    COLORIST,

    @SerialName("Color Separations")
    COLOR_SEPARATIONS,

    @SerialName("Color Assists")
    COLOR_ASSISTS,

    @SerialName("Color Flats")
    COLOR_FLATS,

    @SerialName("Digital Art Technician")
    DIGITAL_ART_TECHNICIAN,

    @SerialName("Gray Tone")
    GRAY_TONE,

    @SerialName("Letterer")
    LETTERER,

    @SerialName("Cover")
    COVER,

    @SerialName("Editor")
    EDITOR,

    @SerialName("Consulting Editor")
    CONSULTING_EDITOR,

    @SerialName("Assistant Editor")
    ASSISTANT_EDITOR,

    @SerialName("Associate Editor")
    ASSOCIATE_EDITOR,

    @SerialName("Group Editor")
    GROUP_EDITOR,

    @SerialName("Senior Editor")
    SENIOR_EDITOR,

    @SerialName("Managing Editor")
    MANAGING_EDITOR,

    @SerialName("Collection Editor")
    COLLECTION_EDITOR,

    @SerialName("Production")
    PRODUCTION,

    @SerialName("Designer")
    DESIGNER,

    @SerialName("Logo Design")
    LOGO_DESIGN,

    @SerialName("Translator")
    TRANSLATOR,

    @SerialName("Supervising Editor")
    SUPERVISING_EDITOR,

    @SerialName("Executive Editor")
    EXECUTIVE_EDITOR,

    @SerialName("Editor In Chief")
    EDITOR_IN_CHIEF,

    @SerialName("President")
    PRESIDENT,

    @SerialName("Publisher")
    PUBLISHER,

    @SerialName("Chief Creative Officer")
    CHIEF_CREATIVE_OFFICER,

    @SerialName("Executive Producer")
    EXECUTIVE_PRODUCER,

    @SerialName("Other")
    OTHER,

    ;

    override fun toString(): String {
        return this.titlecase()
    }
}
