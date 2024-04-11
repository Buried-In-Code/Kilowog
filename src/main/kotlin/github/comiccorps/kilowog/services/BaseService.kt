package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo

abstract class BaseService {
    abstract fun fetch(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
    ): Boolean
}
