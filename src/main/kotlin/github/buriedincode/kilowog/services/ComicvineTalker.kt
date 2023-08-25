package github.buriedincode.kilowog.services

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.metadata.enums.Source
import github.buriedincode.kilowog.services.comicvine.issue.IssueEntry
import github.buriedincode.kilowog.services.comicvine.publisher.PublisherEntry
import github.buriedincode.kilowog.services.comicvine.volume.VolumeEntry
import java.nio.file.Paths
import github.buriedincode.kilowog.Settings.Comicvine as ComicvineSettings

class ComicvineTalker(settings: ComicvineSettings) {
    private val comicvine: Comicvine = Comicvine(
        apiKey = settings.apiKey!!,
        cache = SQLiteCache(path = Paths.get(Utils.CACHE_ROOT.toString(), "cache.sqlite"), expiry = 14),
    )

    private fun searchPublishers(title: String): List<PublisherEntry> {
        val publishers = this.comicvine.listPublishers(title = title)
        if (publishers.isEmpty()) {
            Console.print("No publishers found with query {\"title\": $title}")
        }
        return publishers
    }

    private fun pullPublisher(metadata: Metadata): Int? {
        var publisherId = metadata.issue.publisher.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (publisherId == null) {
            var publisherTitle: String = metadata.issue.publisher.imprint ?: metadata.issue.publisher.title
            do {
                val publishers = this.searchPublishers(title = publisherTitle)
                val index = if (publishers.isNotEmpty()) {
                    Console.menu(
                        choices = publishers.map { "${it.publisherId} - ${it.name}" },
                        prompt = "Select Comicvine Publisher",
                        default = "None of the Above",
                    )
                } else {
                    0
                }
                if (index == 0) {
                    if (Console.confirm(prompt = "Try again")) {
                        publisherTitle = Console.prompt(prompt = "Publisher title") ?: return null
                    }
                } else {
                    publisherId = publishers[index - 1].publisherId
                }
            } while (publisherId == null)
        } else {
            Console.print("Found existing Publisher id")
        }
        val publisher = this.comicvine.getPublisher(publisherId = publisherId) ?: return null
        val resources = metadata.issue.publisher.resources.toMutableSet()
        resources.add(Metadata.Issue.Resource(source = Source.COMICVINE, value = publisherId))
        metadata.issue.publisher.resources = resources.toList()
        metadata.issue.publisher.title = publisher.name

        return publisherId
    }

    private fun searchVolumes(publisherId: Int, title: String, startYear: Int? = null): List<VolumeEntry> {
        val volumes = this.comicvine.listVolumes(publisherId = publisherId, title = title, startYear = startYear)
        if (volumes.isEmpty()) {
            Console.print("No volumes found with query {\"publisherId\": $publisherId, \"title\": $title, \"startYear\": $startYear}")
        }
        return volumes
    }

    private fun pullSeries(metadata: Metadata, publisherId: Int): Int? {
        var volumeId = metadata.issue.series.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (volumeId == null) {
            var volumeTitle: String = metadata.issue.series.title
            var volumeStartYear: Int? = metadata.issue.series.startYear
            do {
                val volumes = this.searchVolumes(publisherId = publisherId, title = volumeTitle, startYear = volumeStartYear)
                val index = if (volumes.isNotEmpty()) {
                    Console.menu(
                        choices = volumes.map { "${it.volumeId} - ${it.name} (${it.startYear})" },
                        prompt = "Select Comicvine Volume",
                        default = "None of the Above",
                    )
                } else {
                    0
                }
                if (index == 0) {
                    if (volumeStartYear != null) {
                        volumeStartYear = null
                    } else if (Console.confirm(prompt = "Try again")) {
                        volumeTitle = Console.prompt(prompt = "Volume title") ?: return null
                    }
                } else {
                    volumeId = volumes[index - 1].volumeId
                }
            } while (volumeId == null)
        } else {
            Console.print("Found existing Volume id")
        }
        val volume = this.comicvine.getVolume(volumeId = volumeId) ?: return null
        val resources = metadata.issue.series.resources.toMutableSet()
        resources.add(Metadata.Issue.Resource(source = Source.COMICVINE, value = volumeId))
        metadata.issue.series.resources = resources.toList()
        metadata.issue.series.startYear = volume.startYear
        metadata.issue.series.title = volume.name

        return volumeId
    }

    private fun searchIssues(volumeId: Int, number: String?): List<IssueEntry> {
        val issues = this.comicvine.listIssues(volumeId = volumeId, number = number)
        if (issues.isEmpty()) {
            Console.print("No issues found with query {\"volumeId\": $volumeId, \"number\": $number}")
        }
        return issues
    }

    private fun pullIssue(metadata: Metadata, seriesId: Int): Int? {
        var issueId = metadata.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (issueId == null) {
            var issueNumber: String? = metadata.issue.number
            do {
                val issues = this.searchIssues(volumeId = seriesId, number = issueNumber)
                val index = if (issues.isNotEmpty()) {
                    Console.menu(
                        choices = issues.map { "${it.issueId} - ${it.name}" },
                        prompt = "Select Comicvine Issue",
                        default = "None of the Above",
                    )
                } else {
                    0
                }
                if (index == 0) {
                    if (Console.confirm(prompt = "Try again")) {
                        issueNumber = Console.prompt(prompt = "Issue number") ?: return null
                    }
                } else {
                    issueId = issues[index - 1].issueId
                }
            } while (issueId == null)
        } else {
            Console.print("Found existing Issue id")
        }
        val issue = this.comicvine.getIssue(issueId = issueId) ?: return null
        val resources = metadata.issue.resources.toMutableSet()
        resources.add(Metadata.Issue.Resource(source = Source.COMICVINE, value = issueId))
        metadata.issue.resources = resources.toList()
        metadata.issue.characters = issue.characters.mapNotNull {
            Metadata.Issue.NamedResource(
                name = it.name ?: return@mapNotNull null,
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.coverDate = issue.coverDate
        metadata.issue.credits = issue.creators.mapNotNull {
            Metadata.Issue.Credit(
                creator = Metadata.Issue.NamedResource(
                    name = it.name ?: return@mapNotNull null,
                    resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
                ),
                roles = it.roles.split("[~\r\n]+".toRegex()).map { it.trim() },
            )
        }
        metadata.issue.locations = issue.locations.mapNotNull {
            Metadata.Issue.NamedResource(
                name = it.name ?: return@mapNotNull null,
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.number = issue.number
        metadata.issue.storeDate = issue.storeDate
        metadata.issue.storyArcs = issue.storyArcs.mapNotNull {
            Metadata.Issue.StoryArc(
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
                title = it.name ?: return@mapNotNull null,
            )
        }
        metadata.issue.summary = issue.summary
        metadata.issue.teams = issue.teams.mapNotNull {
            Metadata.Issue.NamedResource(
                name = it.name ?: return@mapNotNull null,
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.title = issue.name

        return issueId
    }

    fun pullMetadata(metadata: Metadata): Boolean {
        val publisherId = this.pullPublisher(metadata = metadata) ?: return false
        val seriesId = this.pullSeries(metadata = metadata, publisherId = publisherId) ?: return false
        val issueId = this.pullIssue(metadata = metadata, seriesId = seriesId) ?: return false
        return true
    }
}
