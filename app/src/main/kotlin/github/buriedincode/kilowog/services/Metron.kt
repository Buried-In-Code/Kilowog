package github.buriedincode.kilowog.services

import github.buriedincode.kilowog.Details
import github.buriedincode.kilowog.Settings
import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.ComicInfo
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.MetronInfo
import github.buriedincode.metron.SQLiteCache
import github.buriedincode.metron.ServiceException
import github.buriedincode.metron.schemas.BasicIssue
import github.buriedincode.metron.schemas.Issue
import github.buriedincode.metron.schemas.Series
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.div
import github.buriedincode.metron.Metron as Session

class Metron(settings: Settings.Metron) : BaseService<Series, Issue>() {
    private val session = Session(
        username = settings.username!!,
        password = settings.password!!.toString(),
        cache = SQLiteCache(path = (Utils.CACHE_ROOT / "metron.sqlite"), expiry = 14),
    )

    private fun getSeriesByComicvine(comicvineId: Long?): Long? {
        return comicvineId?.let {
            try {
                this.session
                    .listSeries(params = mapOf("cv_id" to it.toString()))
                    .firstOrNull()
                    ?.id
            } catch (se: ServiceException) {
                null
            }
        }
    }

    private fun getSeries(title: String?): Long? {
        val name = title ?: Console.prompt("Series title") ?: return null
        return try {
            val options = this.session.listSeries(params = mapOf("name" to name)).sortedBy { it.name }
            if (options.isEmpty()) {
                LOGGER.warn { "Unable to find any Series with the title: '$name'" }
                return null
            }
            val index = Console.menu(
                choices = options.map { "${it.id} | ${it.name}" + if (it.volume > 1) " v${it.volume}" else "" },
                prompt = "Metron Series",
                default = "None of the Above",
            )
            if (index == 0 && Console.confirm("Try Again")) {
                this.getSeries(title = null)
            } else {
                options.getOrNull(index - 1)?.id
            }
        } catch (se: ServiceException) {
            null
        }
    }

    override fun fetchSeries(details: Details): Series? {
        val seriesId = details.series?.metron
            ?: this.getSeriesByComicvine(comicvineId = details.series?.comicvine)
            ?: this.getSeries(title = details.series?.search)
            ?: return null
        return try {
            this.session.getSeries(id = seriesId).also {
                details.series?.metron = seriesId
            }
        } catch (se: ServiceException) {
            null
        }
    }

    private fun getIssueByComicvine(comicvineId: Long?): Long? {
        return comicvineId?.let {
            try {
                this.session
                    .listIssues(params = mapOf("cv_id" to it.toString()))
                    .firstOrNull()
                    ?.id
            } catch (se: ServiceException) {
                null
            }
        }
    }

    private fun getIssue(seriesId: Long, number: String?): Long? {
        return try {
            val params = mutableMapOf("series_id" to seriesId.toString())
            if (number != null) {
                params["number"] = number
            }
            val options = this.session.listIssues(params = params).sortedWith(compareBy(BasicIssue::number, BasicIssue::name))
            if (options.isEmpty()) {
                LOGGER.warn { "Unable to find any Issues with SeriesId: $seriesId and number: '$number'" }
                return null
            }
            val index = Console.menu(
                choices = options.map { "${it.id} | ${it.name}" },
                prompt = "Metron Issue",
                default = "None of the Above",
            )
            if (index == 0 && number != null) {
                LOGGER.info { "Searching again without the issue number" }
                this.getIssue(seriesId = seriesId, number = null)
            } else {
                options.getOrNull(index - 1)?.id
            }
        } catch (se: ServiceException) {
            null
        }
    }

    override fun fetchIssue(seriesId: Long, details: Details): Issue? {
        val issueId = details.issue?.metron
            ?: this.getIssueByComicvine(comicvineId = details.issue?.comicvine)
            ?: this.getIssue(seriesId = seriesId, number = details.issue?.search)
            ?: return null
        return try {
            this.session.getIssue(id = issueId).also {
                details.issue?.metron = issueId
            }
        } catch (se: ServiceException) {
            null
        }
    }

    private fun processMetadata(series: Series, issue: Issue): Metadata? {
        return null
    }

    private fun processMetronInfo(series: Series, issue: Issue): MetronInfo? {
        return null
    }

    private fun processComicInfo(series: Series, issue: Issue): ComicInfo? {
        return null
    }

    override fun fetch(details: Details): Triple<Metadata?, MetronInfo?, ComicInfo?> {
        if (details.series?.metron == null && details.issue?.metron != null) {
            try {
                this.session.getIssue(id = details.issue.metron!!).also {
                    details.series?.metron = it.series.id
                }
            } catch (se: ServiceException) {
            }
        }

        val series = this.fetchSeries(details = details) ?: return Triple(null, null, null)
        val issue = this.fetchIssue(seriesId = series.id, details = details) ?: return Triple(null, null, null)

        return Triple(
            this.processMetadata(series = series, issue = issue),
            this.processMetronInfo(series = series, issue = issue),
            this.processComicInfo(series = series, issue = issue),
        )
    }

    /*private fun addPublisher(
        publisher: Publisher,
        metadata: Metadata,
    ) {
        val resources = metadata.issue.series.publisher.resources.toMutableSet()
        resources.add(Resource(source = Source.METRON, value = publisher.id))
        metadata.issue.series.publisher.resources = resources.toList()
        metadata.issue.series.publisher.title = publisher.name
    }

    private fun addPublisher(
        publisher: Publisher,
        metronInfo: MetronInfo,
    ) {
        if (metronInfo.id == null || metronInfo.id!!.source == InformationSource.METRON) {
            metronInfo.publisher.id = publisher.id
        }
        metronInfo.publisher.value = publisher.name
    }

    private fun addPublisher(
        publisher: Publisher,
        comicInfo: ComicInfo,
    ) {
        comicInfo.publisher = publisher.name
    }

    private fun addSeries(
        series: Series,
        metadata: Metadata,
    ) {
    }

    private fun addSeries(
        series: Series,
        metronInfo: MetronInfo,
    ) {
    }

    private fun addSeries(
        series: Series,
        comicInfo: ComicInfo,
    ) {
    }

    private fun addIssue(
        issue: Issue,
        metadata: Metadata,
    ) {
    }

    private fun addIssue(
        issue: Issue,
        metronInfo: MetronInfo,
    ) {
    }

    private fun addIssue(
        issue: Issue,
        comicInfo: ComicInfo,
    ) {
    }*/

    companion object {
        @JvmStatic
        private val LOGGER = KotlinLogging.logger { }
    }
}
