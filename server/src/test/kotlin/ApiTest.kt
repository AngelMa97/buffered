package com.angelma

import com.angelma.ext.Throttle
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ApiTest {

    private fun ApplicationTestBuilder.setUp() {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting(
                FakeCatalogRepository(),
                File("build/tmp"),
                Throttle()
            )
        }
    }

    @Test
    fun `health reports the number of videos`() = testApplication {
        setUp()
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"ok","videos":2}""", response.bodyAsText())
    }

    @Test
    fun `videos return videos summaries`() = testApplication {
        setUp()
        val response = client.get("/api/videos")
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "video-a")
        assertContains(response.bodyAsText(), "video-b")
        assertContains(response.bodyAsText(), """"posterUrl":"http://""")
    }

    @Test
    fun `video with unknown id return 404`() = testApplication {
        setUp()
        val response = client.get("/api/videos/invalid-id")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), """video_not_found""")
    }
}