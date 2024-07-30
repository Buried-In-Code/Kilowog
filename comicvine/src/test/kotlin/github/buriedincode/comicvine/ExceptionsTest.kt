package github.buriedincode.comicvine

import github.buriedincode.comicvine.schemas.GenericEntry
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.Duration
import kotlin.jvm.java

@TestInstance(Lifecycle.PER_CLASS)
class ExceptionsTest {
    @Nested
    inner class Authentication {
        @Test
        fun `Test throwing an AuthenticationException`() {
            val session = Comicvine(apiKey = "Invalid", cache = null)
            assertThrows(AuthenticationException::class.java) {
                session.getPublisher(id = 1)
            }
        }
    }

    @Nested
    inner class Service {
        @Test
        fun `Test throwing a ServiceException for a 404`() {
            val apiKey = System.getenv("COMICVINE__API_KEY") ?: "IGNORED"
            val session = Comicvine(apiKey = apiKey, cache = null)
            assertThrows(ServiceException::class.java) {
                val uri = session.encodeURI(endpoint = "/invalid")
                session.getRequest<GenericEntry>(uri = uri)
            }
        }

        @Test
        fun `Test throwing a ServiceException for a timeout`() {
            val apiKey = System.getenv("COMICVINE__API_KEY") ?: "IGNORED"
            val session = Comicvine(apiKey = apiKey, cache = null, timeout = Duration.ofMillis(1))
            assertThrows(ServiceException::class.java) {
                session.getPublisher(id = 1)
            }
        }
    }
}
