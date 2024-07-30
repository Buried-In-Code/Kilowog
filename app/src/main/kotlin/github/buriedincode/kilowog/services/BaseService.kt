package github.buriedincode.kilowog.services

import github.buriedincode.kilowog.Details
import github.buriedincode.kilowog.models.ComicInfo
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.MetronInfo

abstract class BaseService<S, I> {
    abstract fun fetchSeries(details: Details): S?

    abstract fun fetchIssue(seriesId: Long, details: Details): I?

    abstract fun fetch(details: Details): Triple<Metadata?, MetronInfo?, ComicInfo?>
}
