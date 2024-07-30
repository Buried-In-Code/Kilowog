package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.archives.BaseArchive
import kotlinx.serialization.ExperimentalSerializationApi
import java.nio.file.Path

abstract class InfoModel {
    abstract fun toFile(file: Path)

    abstract class Companion {
        @OptIn(ExperimentalSerializationApi::class)
        abstract fun fromArchive(archive: BaseArchive): InfoModel?
    }
}
