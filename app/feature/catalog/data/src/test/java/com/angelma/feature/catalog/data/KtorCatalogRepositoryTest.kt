package com.angelma.feature.catalog.data

import com.angelma.core.domain.util.DataError
import com.angelma.core.domain.util.Result
import com.angelma.feature.catalog.data.repository.KtorCatalogRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorCatalogRepositoryTest {

    private fun repositoryRespondingWith(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK
    ): KtorCatalogRepository {
        val engine = MockEngine { _ ->
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return KtorCatalogRepository(client)
    }

    @Test
    fun `getVideos maps the list`() = runTest {
        val repository = repositoryRespondingWith(
            """[{"id":"sintel","title":"Sintel","year":2010,"durationSeconds":60,
                  "posterUrl":"http://x/poster.jpg"}]"""
        )

        val result = repository.getVideos()

        assertTrue(result is Result.Success)
        assertEquals("sintel", (result as Result.Success).data.first().id)
    }

    @Test
    fun `getVideo fails id not found`() = runTest {
        val repository = repositoryRespondingWith(
            body = """{"error":"video_not_found","message":"No video with id 'incorrect-id'"}""",
            status = HttpStatusCode.NotFound
        )

        val result = repository.getVideo("incorrect-id")

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.NOT_FOUND, result.error)
    }

    @Test
    fun `getVideo fails for bad serialization`() = runTest {
        val repository = repositoryRespondingWith(
            """{"status":"ok","videos":7}"""
        )

        val result = repository.getVideo("incorrect-id")

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.SERIALIZATION, result.error)
    }
}