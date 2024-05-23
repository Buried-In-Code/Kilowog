package github.comiccorps.metron.schemas

import github.comiccorps.metron.Metron
import github.comiccorps.metron.SQLiteCache
import github.comiccorps.metron.ServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class SeriesTest {
    private val session: Metron

    init {
        val username = System.getenv("METRON__USERNAME") ?: "IGNORED"
        val password = System.getenv("METRON__PASSWORD") ?: "IGNORED"
        val cache = SQLiteCache(path = Paths.get("cache.sqlite"), expiry = null)
        session = Metron(username = username, password = password, cache = cache)
    }

    @Nested
    inner class ListSeries {
        @Test
        fun `Test ListSeries with a valid search`() {
            val results = session.listSeries(params = mapOf("name" to "Bone"))
            assertEquals(8, results.size)
            assertAll(
                { assertEquals(119, results[0].id) },
                { assertEquals(56, results[0].issueCount) },
                { assertEquals("Bone (1991)", results[0].name) },
                { assertEquals(1, results[0].volume) },
                { assertEquals(1991, results[0].yearBegan) },
            )
        }

        @Test
        fun `Test ListSeries with an invalid search`() {
            val results = session.listSeries(params = mapOf("name" to "INVALID"))
            assertTrue(results.isEmpty())
        }
    }

    @Nested
    inner class GetSeries {
        @Test
        fun `Test GetSeries with a valid id`() {
            val result = session.getSeries(id = 119)
            assertNotNull(result)
            assertAll(
                { assertTrue(result.associated.isEmpty()) },
                { assertEquals(4691, result.comicvineId) },
                { assertTrue(result.genres.isEmpty()) },
                { assertEquals(119, result.id) },
                { assertEquals(56, result.issueCount) },
                { assertEquals("Bone", result.name) },
                { assertEquals(19, result.publisher.id) },
                { assertEquals("Cartoon Books", result.publisher.name) },
                { assertEquals("https://metron.cloud/series/bone-1991/", result.resourceUrl) },
                { assertEquals(2, result.seriesType.id) },
                { assertEquals("Cancelled Series", result.seriesType.name) },
                { assertEquals("Bone", result.sortName) },
                { assertEquals(1, result.volume) },
                { assertEquals(1991, result.yearBegan) },
                { assertEquals(1995, result.yearEnd) },
            )
        }

        @Test
        fun `Test GetSeries with an invalid id`() {
            assertThrows(ServiceException::class.java) {
                session.getSeries(id = -1)
            }
        }
    }
}
