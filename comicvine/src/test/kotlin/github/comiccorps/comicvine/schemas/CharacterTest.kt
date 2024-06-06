package github.comiccorps.comicvine.schemas

import github.comiccorps.comicvine.Comicvine
import github.comiccorps.comicvine.SQLiteCache
import github.comiccorps.comicvine.ServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertAll
import java.nio.file.Paths
import kotlin.jvm.java
import kotlin.to

@TestInstance(Lifecycle.PER_CLASS)
class CharacterTest {
    private val session: Comicvine

    init {
        val apiKey = System.getenv("COMICVINE__API_KEY") ?: "IGNORED"
        val cache = SQLiteCache(path = Paths.get("cache.sqlite"), expiry = null)
        session = Comicvine(apiKey = apiKey, cache = cache)
    }

    @Nested
    inner class ListCharacters {
        @Test
        fun `Test ListCharacters with a valid search`() {
            val results = session.listCharacters(params = mapOf("filter" to "name:Smiley Bone"))
            assertEquals(1, results.size)
            assertAll(
                { assertNull(results[0].aliases) },
                { assertEquals("https://comicvine.gamespot.com/api/character/4005-23092/", results[0].apiUrl) },
                { assertNull(results[0].birth) },
                { assertEquals(146974, results[0].firstIssue?.id) },
                { assertEquals(1, results[0].gender) },
                { assertEquals(23092, results[0].id) },
                { assertEquals(125, results[0].issueCount) },
                { assertEquals("Smiley Bone", results[0].name) },
                { assertEquals(9, results[0].origin?.id) },
                { assertEquals(490, results[0].publisher?.id) },
                { assertNull(results[0].realName) },
                { assertEquals("https://comicvine.gamespot.com/smiley-bone/4005-23092/", results[0].siteUrl) },
            )
        }

        @Test
        fun `Test ListCharacters with an invalid search`() {
            val results = session.listCharacters(params = mapOf("filter" to "name:INVALID"))
            assertTrue(results.isEmpty())
        }
    }

    @Nested
    inner class GetCharacter {
        @Test
        fun `Test GetCharacter with a valid id`() {
            val result = session.getCharacter(id = 23092)
            assertNotNull(result)
            assertAll(
                { assertNull(result.aliases) },
                { assertEquals("https://comicvine.gamespot.com/api/character/4005-23092/", result.apiUrl) },
                { assertNull(result.birth) },
                { assertEquals(146974, result.firstIssue?.id) },
                { assertEquals(1, result.gender) },
                { assertEquals(23092, result.id) },
                { assertEquals(125, result.issueCount) },
                { assertEquals("Smiley Bone", result.name) },
                { assertEquals(9, result.origin?.id) },
                { assertEquals(490, result.publisher?.id) },
                { assertNull(result.realName) },
                { assertEquals("https://comicvine.gamespot.com/smiley-bone/4005-23092/", result.siteUrl) },
                { assertEquals(23088, result.creators.firstOrNull()?.id) },
                { assertTrue(result.deaths.isEmpty()) },
                { assertEquals(25988, result.enemies.firstOrNull()?.id) },
                { assertTrue(result.enemyTeams.isEmpty()) },
                { assertTrue(result.friendlyTeams.isEmpty()) },
                { assertEquals(23560, result.friends.firstOrNull()?.id) },
                { assertEquals(790057, result.issues.firstOrNull()?.id) },
                { assertTrue(result.powers.isEmpty()) },
                { assertTrue(result.storyArcs.isEmpty()) },
                { assertEquals(62268, result.teams.firstOrNull()?.id) },
                { assertEquals(85646, result.volumes.firstOrNull()?.id) },
            )
        }

        @Test
        fun `Test GetCharacter with an invalid id`() {
            assertThrows(ServiceException::class.java) {
                session.getCharacter(id = -1)
            }
        }
    }
}
