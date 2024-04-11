package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.Settings
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo
import org.apache.logging.log4j.kotlin.Logging

class MarvelTalker(settings: Settings.Marvel) : BaseService() {
    override fun fetch(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
    ): Boolean {
        if (metadata == null && metronInfo == null && comicInfo == null) {
            return false
        }

        TODO()
    }

    companion object : Logging
}
