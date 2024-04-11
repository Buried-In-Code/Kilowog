package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.Settings
import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.Utils.asEnumOrNull
import github.comiccorps.kilowog.Utils.associateNotNull
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo
import github.comiccorps.kilowog.models.metadata.Credit
import github.comiccorps.kilowog.models.metadata.Resource
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metadata.StoryArc
import github.comiccorps.kilowog.models.metadata.TitledResource
import github.comiccorps.kilowog.models.metroninfo.Arc
import github.comiccorps.kilowog.models.metroninfo.InformationSource
import github.comiccorps.kilowog.models.metroninfo.Role
import github.comiccorps.kilowog.models.metroninfo.RoleResource
import github.comiccorps.kilowog.services.comicvine.issue.Issue
import github.comiccorps.kilowog.services.comicvine.issue.IssueEntry
import github.comiccorps.kilowog.services.comicvine.publisher.Publisher
import github.comiccorps.kilowog.services.comicvine.publisher.PublisherEntry
import github.comiccorps.kilowog.services.comicvine.volume.Volume
import github.comiccorps.kilowog.services.comicvine.volume.VolumeEntry
import kotlinx.datetime.toKotlinLocalDate
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import github.comiccorps.kilowog.models.metroninfo.Credit as MetronCredit
import github.comiccorps.kilowog.models.metroninfo.Resource as MetronResource
import github.comiccorps.kilowog.models.metroninfo.Source as MetronSource

class ComicvineTalker(settings: Settings.Comicvine) : BaseService() {
    private val session: Comicvine = Comicvine(
        apiKey = settings.apiKey!!,
        cache = SQLiteCache(path = Paths.get(Utils.CACHE_ROOT.toString(), "cache.sqlite"), expiry = 14),
    )

    private fun searchPublishers(title: String?): Long? {
        val name = title ?: Console.prompt("Publisher title")
        val options = this.session.listPublishers(mapOf("filter" to "name:$name")).sortedWith(compareBy(PublisherEntry::name))
        val index = Console.menu(
            choices = options.map { "${it.id} | ${it.name}" },
            prompt = "Select Comicvine Publisher",
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
        resources.add(Resource(source = Source.COMICVINE, value = publisher.id))
        metadata.issue.series.publisher.resources = resources.toList()
        metadata.issue.series.publisher.title = publisher.name
    }

    private fun addPublisher(
        publisher: Publisher,
        metronInfo: MetronInfo,
    ) {
        if (metronInfo.id == null || metronInfo.id!!.source == InformationSource.COMIC_VINE) {
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
        metadata?.let {
            publisherId = it.issue.series.publisher.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        }
        if (publisherId == null) {
            metronInfo?.let {
                if (it.id != null && it.id!!.source == InformationSource.COMIC_VINE) {
                    publisherId = it.publisher.id
                }
            }
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

    private fun searchVolumes(
        publisherId: Long,
        title: String?,
    ): Long? {
        val name = title ?: Console.prompt("Series title")
        val options = this.session.listVolumes(mapOf("filter" to "name:$name")).filter {
            it.publisher != null && it.publisher.id == publisherId
        }.sortedWith(compareBy(VolumeEntry::name, VolumeEntry::startYear))
        val index = Console.menu(
            choices = options.map { "${it.id} | ${it.name} (${it.startYear})" },
            prompt = "Select Comicvine Volume",
            default = "None of the Above",
        )
        if (index != 0) {
            return options[index - 1].id
        }
        if (!Console.confirm(prompt = "Try Again")) {
            return null
        }
        return this.searchVolumes(publisherId = publisherId, title = null)
    }

    private fun addVolume(
        volume: Volume,
        metadata: Metadata,
    ) {
        val resources = metadata.issue.series.resources.toMutableSet()
        resources.add(Resource(source = Source.COMICVINE, value = volume.id))
        metadata.issue.series.resources = resources.toList()
        metadata.issue.series.startYear = volume.startYear
        metadata.issue.series.title = volume.name
    }

    private fun addVolume(
        volume: Volume,
        metronInfo: MetronInfo,
    ) {
        if (metronInfo.id == null || metronInfo.id!!.source == InformationSource.COMIC_VINE) {
            metronInfo.series.id = volume.id
        }
        metronInfo.series.name = volume.name
    }

    private fun addVolume(
        volume: Volume,
        comicInfo: ComicInfo,
    ) {
        comicInfo.series = volume.name
    }

    fun fetchSeries(
        metadata: Metadata?,
        metronInfo: MetronInfo?,
        comicInfo: ComicInfo?,
        publisherId: Long,
    ): Volume? {
        var volumeId: Long? = null
        metadata?.let {
            volumeId = it.issue.series.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        }
        if (volumeId == null) {
            metronInfo?.let {
                if (it.id != null && it.id!!.source == InformationSource.COMIC_VINE) {
                    volumeId = it.series.id
                }
            }
        }
        if (volumeId == null) {
            volumeId = this.searchVolumes(
                publisherId = publisherId,
                title = metadata?.issue?.series?.title ?: metronInfo?.series?.name ?: comicInfo?.series,
            ) ?: return null
        }

        val volume = this.session.getVolume(volumeId = volumeId!!) ?: return null
        metadata?.let {
            this.addVolume(volume = volume, metadata = it)
        }
        metronInfo?.let {
            this.addVolume(volume = volume, metronInfo = it)
        }
        comicInfo?.let {
            this.addVolume(volume = volume, comicInfo = it)
        }
        return volume
    }

    private fun searchIssues(
        volumeId: Long,
        number: String?,
    ): Long? {
        val options = if (number == null) {
            this.session.listIssues(mapOf("filter" to "volume:$volumeId")).sortedWith(compareBy(IssueEntry::number, IssueEntry::name))
        } else {
            this.session.listIssues(
                mapOf("filter" to "volume:$volumeId,issue_number:$number"),
            ).sortedWith(compareBy(IssueEntry::number, IssueEntry::name))
        }
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
        return this.searchIssues(volumeId = volumeId, number = null)
    }

    private fun addIssue(
        issue: Issue,
        metadata: Metadata,
    ) {
        val resources = metadata.issue.resources.toMutableSet()
        resources.add(Resource(source = Source.COMICVINE, value = issue.id))
        metadata.issue.resources = resources.toList()
        metadata.issue.characters = issue.characters.mapNotNull {
            TitledResource(
                title = it.name ?: return@mapNotNull null,
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.coverDate = issue.coverDate
        metadata.issue.credits = issue.creators.mapNotNull {
            Credit(
                creator = TitledResource(
                    title = it.name ?: return@mapNotNull null,
                    resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
                ),
                roles = it.roles.split("[~\r\n]+".toRegex()).map { TitledResource(title = it.trim()) },
            )
        }
        metadata.issue.locations = issue.locations.mapNotNull {
            TitledResource(
                title = it.name ?: return@mapNotNull null,
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.number = issue.number
        metadata.issue.storeDate = issue.storeDate
        metadata.issue.storyArcs = issue.storyArcs.mapNotNull {
            StoryArc(
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
                title = it.name ?: return@mapNotNull null,
            )
        }
        metadata.issue.summary = issue.summary
        metadata.issue.teams = issue.teams.mapNotNull {
            TitledResource(
                title = it.name ?: return@mapNotNull null,
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.title = issue.name
    }

    private fun addIssue(
        issue: Issue,
        metronInfo: MetronInfo,
    ) {
        metronInfo.arcs = issue.storyArcs.mapNotNull {
            Arc(
                id = it.id,
                name = it.name ?: return@mapNotNull null,
            )
        }
        metronInfo.characters = issue.characters.mapNotNull {
            MetronResource(
                id = it.id,
                value = it.name ?: return@mapNotNull null,
            )
        }
        metronInfo.coverDate = issue.coverDate ?: LocalDate.parse(
            Console.prompt("Cover Date (yyyy-mm-dd)"),
            DateTimeFormatter.ISO_DATE,
        ).toKotlinLocalDate()
        metronInfo.credits = issue.creators.mapNotNull {
            MetronCredit(
                creator = it.name?.let { name -> MetronResource(id = it.id, value = name) } ?: return@mapNotNull null,
                roles = it.roles.split("[~\r\n]+".toRegex()).map { RoleResource(value = it.trim().asEnumOrNull<Role>() ?: Role.OTHER) },
            )
        }
        if (metronInfo.id == null || metronInfo.id!!.source == InformationSource.COMIC_VINE) {
            metronInfo.id = MetronSource(source = InformationSource.COMIC_VINE, value = issue.id)
        }
        metronInfo.locations = issue.locations.mapNotNull {
            MetronResource(
                id = it.id,
                value = it.name ?: return@mapNotNull null,
            )
        }
        metronInfo.number = issue.number
        metronInfo.storeDate = issue.storeDate
        metronInfo.summary = issue.summary
        metronInfo.teams = issue.teams.mapNotNull {
            MetronResource(
                id = it.id,
                value = it.name ?: return@mapNotNull null,
            )
        }
        metronInfo.title = issue.name
    }

    private fun addIssue(
        issue: Issue,
        comicInfo: ComicInfo,
    ) {
        comicInfo.characterList = issue.characters.mapNotNull { it.name }
        comicInfo.credits = issue.creators.associateNotNull {
            val roles = it.roles.split("[~\r\n]+".toRegex()).map { it.trim() }
            if (it.name.isNullOrBlank() || roles.isEmpty()) {
                null to null
            } else {
                it.name to roles
            }
        }
        comicInfo.coverDate = issue.coverDate
        comicInfo.locationList = issue.locations.mapNotNull { it.name }
        comicInfo.number = issue.number
        comicInfo.storyArcList = issue.storyArcs.mapNotNull { it.name }
        comicInfo.summary = issue.summary
        comicInfo.teamList = issue.teams.mapNotNull { it.name }
        comicInfo.title = issue.name
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
                volumeId = seriesId,
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
