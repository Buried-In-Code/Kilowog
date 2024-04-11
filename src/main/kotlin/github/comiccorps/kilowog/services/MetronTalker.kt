package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.Settings
import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo
import github.comiccorps.kilowog.models.metadata.Resource
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metroninfo.InformationSource
import github.comiccorps.kilowog.services.metron.issue.Issue
import github.comiccorps.kilowog.services.metron.issue.IssueEntry
import github.comiccorps.kilowog.services.metron.publisher.Publisher
import github.comiccorps.kilowog.services.metron.publisher.PublisherEntry
import github.comiccorps.kilowog.services.metron.series.Series
import github.comiccorps.kilowog.services.metron.series.SeriesEntry
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Paths

class MetronTalker(settings: Settings.Metron) : BaseService() {
    private val session: Metron = Metron(
        username = settings.username!!,
        password = settings.password!!,
        cache = SQLiteCache(path = Paths.get(Utils.CACHE_ROOT.toString(), "cache.sqlite"), expiry = 14),
    )

    private fun searchPublishers(title: String?): Long? {
        val name = title ?: Console.prompt("Publisher title")
        val options = if (name == null)
            this.session.listPublishers()
        else
            this.session.listPublishers(mapOf("name" to name))
        val index = Console.menu(
            choices = options.map { "${it.id} | ${it.name}" },
            prompt = "Select Metron Publisher",
            default = "None of the Above",
        )
        if (index != 0) {
            return options[index - 1].id
        }
        if (!Console.confirm(prompt = "Try Again")) {
            return null
        }
        return this.searchPublishers(title = null)
    }

    private fun addPublisher(
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

    fun fetchPublisher(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
    ): Publisher? {
        var publisherId: Long? = null
        var comicvineId: Long? = null
        metadata?.let {
            publisherId = it.issue.series.publisher.resources.firstOrNull { it.source == Source.METRON }?.value
            comicvineId = it.issue.series.publisher.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        }
        if (publisherId == null) {
            metronInfo?.let {
                if (it.id != null && it.id!!.source == InformationSource.METRON) {
                    publisherId = it.publisher.id
                }
            }
        }
        if (comicvineId == null) {
            metronInfo?.let {
                if (it.id != null && it.id!!.source == InformationSource.COMIC_VINE)
                    comicvineId = it.publisher.id
            }
        }
        if (publisherId == null && comicvineId != null) {
            publisherId = this.session.getPublisherByComicvine(comicvineId = comicvineId!!)?.id
        }
        if (publisherId == null) {
            publisherId = this.searchPublishers(
                title = metadata?.issue?.series?.publisher?.title ?: metronInfo?.publisher?.value ?: comicInfo?.publisher,
            ) ?: return null
        }

        val publisher = this.session.getPublisher(publisherId = publisherId!!) ?: return null
        metadata?.let {
            this.addPublisher(publisher = publisher, metadata = it)
        }
        metronInfo?.let {
            this.addPublisher(publisher = publisher, metronInfo = it)
        }
        comicInfo?.let {
            this.addPublisher(publisher = publisher, comicInfo = it)
        }
        return publisher
    }

    private fun searchSeries(
        publisherId: Long,
        title: String?,
    ): Long? {
        val name = title ?: Console.prompt("Series title")
        val options = if (name == null)
            this.session.listSeries(mapOf("publisher_id" to publisherId.toString()))
        else
            this.session.listSeries(mapOf("publisher_id" to publisherId.toString(), "name" to name))
        val index = Console.menu(
            choices = options.map { "${it.id} | ${it.name} (${it.yearBegan})" },
            prompt = "Select Metron Series",
            default = "None of the Above",
        )
        if (index != 0) {
            return options[index - 1].id
        }
        if (!Console.confirm(prompt = "Try Again")) {
            return null
        }
        return this.searchSeries(publisherId = publisherId, title = null)
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

    fun fetchSeries(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
        publisherId: Long,
    ): Series? {
        var seriesId: Long? = null
        metadata?.let {
            seriesId = it.issue.series.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        }
        if (seriesId == null) {
            metronInfo?.let {
                if (it.id != null && it.id!!.source == InformationSource.COMIC_VINE) {
                    seriesId = it.series.id
                }
            }
        }
        if (seriesId == null) {
            seriesId = this.searchSeries(
                publisherId = publisherId,
                title = metadata?.issue?.series?.title ?: metronInfo?.series?.name ?: comicInfo?.series,
            ) ?: return null
        }

        val series = this.session.getSeries(seriesId = seriesId!!) ?: return null
        metadata?.let {
            this.addSeries(series = series, metadata = it)
        }
        metronInfo?.let {
            this.addSeries(series = series, metronInfo = it)
        }
        comicInfo?.let {
            this.addSeries(series = series, comicInfo = it)
        }
        return series
    }

    private fun searchIssues(
        seriesId: Long,
        number: String?,
    ): Long? {
        val options = emptyList<IssueEntry>()
        val index = Console.menu(
            choices = options.map { "${it.id} | ${it.number} - ${it.name}" },
            prompt = "Select Comicvine Issue",
            default = "None of the Above",
        )
        if (index != 0) {
            return options[index - 1].id
        }
        if (number == null) {
            return null
        }
        if (!Console.confirm(prompt = "Try Again")) {
            return null
        }
        return this.searchIssues(seriesId = seriesId, number = null)
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
    }

    fun fetchIssue(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
        seriesId: Long,
    ): Issue? {
        var issueId: Long? = null
        metadata?.let {
            issueId = it.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        }
        if (issueId == null) {
            metronInfo?.let {
                if (it.id != null && it.id!!.source == InformationSource.COMIC_VINE) {
                    issueId = it.id!!.value
                }
            }
        }
        if (issueId == null) {
            issueId = this.searchIssues(
                seriesId = seriesId,
                number = metadata?.issue?.number ?: metronInfo?.number ?: comicInfo?.number,
            ) ?: return null
        }

        val issue = this.session.getIssue(issueId = issueId!!) ?: return null
        metadata?.let {
            this.addIssue(issue = issue, metadata = it)
        }
        metronInfo?.let {
            this.addIssue(issue = issue, metronInfo = it)
        }
        comicInfo?.let {
            this.addIssue(issue = issue, comicInfo = it)
        }
        return issue
    }

    override fun fetch(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
    ): Boolean {
        if (metadata == null && metronInfo == null && comicInfo == null) {
            return false
        }

        val publisher = this.fetchPublisher(metadata = metadata, metronInfo = metronInfo, comicInfo = comicInfo) ?: return false
        val series = this.fetchSeries(
            metadata = metadata,
            metronInfo = metronInfo,
            comicInfo = comicInfo,
            publisherId = publisher.id,
        ) ?: return false
        val issue = this.fetchIssue(
            metadata = metadata,
            metronInfo = metronInfo,
            comicInfo = comicInfo,
            seriesId = series.id,
        ) ?: return false
        return true
    }

    companion object : Logging
}
