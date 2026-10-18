package com.matchparfum.data.remote

import com.matchparfum.data.model.BrandsResponse
import com.matchparfum.data.model.Gender
import com.matchparfum.data.model.PerfumeDetail
import com.matchparfum.data.model.ProblemDetails
import com.matchparfum.data.model.RecommendationResponse
import com.matchparfum.data.model.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Client for perfume-api `/v1`. All calls throw [ApiException] on failure. */
class PerfumeApi(private val client: HttpClient) {

    suspend fun search(query: String, limit: Int = DEFAULT_LIMIT): SearchResponse =
        call {
            client.get("v1/perfumes/search") {
                parameter("q", query)
                parameter("limit", limit)
            }
        }

    /** Most-voted perfumes, optionally for one [gender]. */
    suspend fun popular(limit: Int = DEFAULT_LIMIT, gender: Gender? = null): SearchResponse =
        call {
            client.get("v1/perfumes/popular") {
                parameter("limit", limit)
                gender?.let { parameter("gender", it.apiValue) }
            }
        }

    suspend fun perfume(id: Long): PerfumeDetail =
        call { client.get("v1/perfumes/$id") }

    suspend fun recommendations(
        id: Long,
        limit: Int = DEFAULT_LIMIT,
        gender: Gender? = null,
        /** Only recommend perfumes from this brand (exact name, case-insensitive). */
        brand: String? = null,
    ): RecommendationResponse =
        call {
            client.get("v1/perfumes/$id/recommendations") {
                parameter("limit", limit)
                gender?.let { parameter("gender", it.apiValue) }
                brand?.let { parameter("brand", it) }
            }
        }

    /** Brands whose name contains [query], those with the most perfumes first. */
    suspend fun brands(query: String, limit: Int = 10): BrandsResponse =
        call {
            client.get("v1/brands") {
                parameter("q", query)
                parameter("limit", limit)
            }
        }

    // Off the main thread: decoding a response (e.g. 30 recommendations) on it drops frames when
    // the response lands during a screen transition.
    private suspend inline fun <reified T> call(crossinline request: suspend () -> HttpResponse): T =
        withContext(Dispatchers.Default) { execute { request() } }

    private suspend inline fun <reified T> execute(request: () -> HttpResponse): T {
        val response = try {
            request()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // IOException, timeouts and engine-specific connection errors (not all are IOException).
            throw ApiException.Network(e)
        }

        if (!response.status.isSuccess()) throw response.toApiException()

        return try {
            response.body()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ApiException.Unexpected(e)
        }
    }

    private suspend fun HttpResponse.toApiException(): ApiException.Http {
        val text = runCatching { bodyAsText() }.getOrDefault("")
        val problem = try {
            ApiJson.decodeFromString<ProblemDetails>(text)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
        return problem?.let { ApiException.Http(it) }
            ?: ApiException.Http(
                status = status.value,
                code = "http_${status.value}",
                detail = null,
                requestId = headers["X-Request-ID"],
            )
    }

    companion object {
        const val DEFAULT_LIMIT = 20

        /** Server caps `limit` at 50 (see `Limit` in app/routes.py). */
        const val MAX_LIMIT = 50

        /** Server requires `q` to have at least 3 characters after trimming. */
        const val MIN_QUERY_LENGTH = 3
    }
}

val ApiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

fun createHttpClient(baseUrl: String, engine: HttpClientEngine = httpEngine().create()): HttpClient =
    HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(ApiJson)
            // Errors come as application/problem+json; register it too.
            json(ApiJson, ContentType("application", "problem+json"))
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 15_000
        }
        defaultRequest {
            url(baseUrl.trimEnd('/') + "/")
        }
    }
