package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.Details
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo

abstract class BaseService<S, I> {
    abstract fun fetchSeries(details: Details): S?

    abstract fun fetchIssue(seriesId: Long, details: Details): I?

    abstract fun fetch(details: Details): Triple<Metadata?, MetronInfo?, ComicInfo?>
}
