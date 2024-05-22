package github.comiccorps.kilowog.services

import github.buriedincode.kalibak.ServiceException
import github.buriedincode.kalibak.schemas.Series
import github.comiccorps.kilowog.Settings
import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo
import github.comiccorps.kilowog.models.metadata.Resource
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metroninfo.InformationSource
import org.apache.logging.log4j.kotlin.Logging
import github.buriedincode.kalibak.Metron as Kalibak

class Metron(settings: Settings.Metron) : BaseService() {
    private val session = Kalibak(
        username = settings.username!!,
        password = settings.password!!,
        cache = SQLiteCache(path = (Utils.CACHE_ROOT / "kalibak.sqlite"), expiry = 14),
    )

    private fun getSeriesByComicvine(comicvineId: Long?): Long? {
        return comicvineId?.let {
            try {
                this.session.listSeries(params = mapOf("cv_id" to it)).firstOrNull()?.id
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
                logger.warn("Unable to find any Series with the title: '$title'")
                return null
            }
            val index = Console.menu(
                choices = options.map { if (it.volume > 1) "${it.id} | ${it.name} v${it.volume}" else "${it.id} | ${it.name}" },
                prompt = "Metron Series",
                default = "None of the Above",
            )
            if (index == 0 && Console.confirm("Try Again")) {
                getSeries(title = null)
            } else {
                options.getOrNull(index - 1)?.id
            }
        } catch (se: ServiceException) {
            null
        }
    }

    fun fetchSeries(details: Details): Series? {
        val seriesId = details.series.metron
            ?: getSeriesByComicvine(comicvineId = details.series.comicvine)
            ?: getSeries(title = details.series.search)
            ?: return null
        return try {
            this.session.getSeries(id = seriesId).also {
                details.series.metron = seriesId
            }
        } catch (se: ServiceException) {
            null
        }
    }
    
    private fun getIssueByComicvine(comicvineId: Long?): Long? {
        return comicvineId?.let {
            try {
                this.session.listIssues(params = mapOf("cv_id" to it)).firstOrNull()?.id
            } catch (se: ServiceException) {
                null
            }
        }
    }

    private fun getIssue(seriesId: Long, number: String?): Long? {
        val name = title ?: Console.prompt("Series title") ?: return null
        return try {
            val options = this.session.listSeries(params = mapOf("name" to name)).sortedBy { it.name }
            if (options.isEmpty()) {
                logger.warn("Unable to find any Series with the title: '$title'")
                return null
            }
            val index = Console.menu(
                choices = options.map { if (it.volume > 1) "${it.id} | ${it.name} v${it.volume}" else "${it.id} | ${it.name}" },
                prompt = "Metron Series",
                default = "None of the Above",
            )
            if (index == 0 && Console.confirm("Try Again")) {
                getSeries(title = null)
            } else {
                options.getOrNull(index - 1)?.id
            }
        } catch (se: ServiceException) {
            null
        }
    }

    fun fetchIssue(seriesId: Long, details: Details): Issue? {
        val issueId = details.issue.metron
            ?: getIssueByComicvine(comicvineId = details.issue.comicvine)
            ?: getIssue(seriesId= seriesId, number = details.issue.search)
            ?: return null
        return try {
            this.session.getIssue(id = issueId).also {
                details.issue.metron = issueId
            }
        } catch (se: ServiceException) {
            null
        }
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
                if (it.id != null && it.id!!.source == InformationSource.COMIC_VINE) {
                    comicvineId = it.publisher.id
                }
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
        val options = if (name == null) {
            this.session.listSeries(mapOf("publisher_id" to publisherId.toString()))
        } else {
            this.session.listSeries(mapOf("publisher_id" to publisherId.toString(), "name" to name))
        }
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
