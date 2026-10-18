package com.matchparfum.data.remote

import com.matchparfum.data.model.Gender
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PerfumeApiTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun api(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        PerfumeApi(
            createHttpClient(
                baseUrl = "http://api.test",
                engine = MockEngine { request ->
                    requests += request
                    handler(request)
                },
            ),
        )

    private fun MockRequestHandleScope.json(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        contentType: String = "application/json",
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, contentType))

    @Test
    fun searchSendsQueryAndParsesSummaries() = runTest {
        val api = api {
            json(
                """{"items":[{"id":26136,"slug":"Natura/Kaiak-Urbe","name":"Kaiak Urbe","brand":"Natura",
                "year":2012,"gender":"male","url":"https://x","rating_avg":4.1,"vote_count":321}]}""",
            )
        }

        val result = api.search("kaiak", limit = 5)

        val url = requests.single().url
        assertEquals("/v1/perfumes/search", url.encodedPath)
        assertEquals("kaiak", url.parameters["q"])
        assertEquals("5", url.parameters["limit"])
        val item = result.items.single()
        assertEquals(26136L, item.id)
        assertEquals(Gender.Male, item.gender)
        assertEquals(321, item.voteCount)
    }

    @Test
    fun popularSendsGenderAndLimit() = runTest {
        val api = api { json("""{"items":[{"id":1,"slug":"s","name":"Sauvage","brand":"Dior"}]}""") }

        val result = api.popular(limit = 5, gender = Gender.Unisex)

        val url = requests.single().url
        assertEquals("/v1/perfumes/popular", url.encodedPath)
        assertEquals("5", url.parameters["limit"])
        assertEquals("unisex", url.parameters["gender"])
        assertEquals("Sauvage", result.items.single().name)
    }

    @Test
    fun recommendationsSendBrandFilter() = runTest {
        val api = api { json("""{"query":{"id":7,"slug":"s","name":"Q","brand":"B"},"recommendations":[]}""") }

        api.recommendations(7, brand = "O Boticário")

        assertEquals("O Boticário", requests.single().url.parameters["brand"])
    }

    @Test
    fun brandsSendQueryAndParseCounts() = runTest {
        val api = api { json("""{"items":[{"name":"Natura","perfume_count":553}]}""") }

        val result = api.brands("natu", limit = 8)

        val url = requests.single().url
        assertEquals("/v1/brands", url.encodedPath)
        assertEquals("natu", url.parameters["q"])
        assertEquals("8", url.parameters["limit"])
        assertEquals(553, result.items.single().perfumeCount)
    }

    @Test
    fun popularWithoutGenderOmitsParameter() = runTest {
        val api = api { json("""{"items":[]}""") }

        api.popular()

        assertNull(requests.single().url.parameters["gender"])
    }

    @Test
    fun detailToleratesNullsAndUnknownFields() = runTest {
        val api = api {
            json(
                """{"id":1,"slug":"a/b","name":"Orange Tonic","brand":"Azzaro","year":null,"gender":null,
                "url":null,"rating_avg":null,"vote_count":0,"collection":null,"longevity_avg":2.87,
                "sillage_avg":null,"price_value_avg":null,"future_field":true,
                "accords":[{"name":"citrus","weight":100.0}],"notes_top":[{"name":"Orange","weight":100}],
                "notes_middle":[],"notes_base":[]}""",
            )
        }

        val detail = api.perfume(1)

        assertEquals("/v1/perfumes/1", requests.single().url.encodedPath)
        assertNull(detail.gender)
        assertEquals("citrus", detail.accords.single().name)
        assertEquals(2.87, detail.longevityAvg)
    }

    @Test
    fun recommendationsSendGenderFilter() = runTest {
        val api = api {
            json(
                """{"query":{"id":7,"slug":"s","name":"Q","brand":"B"},
                "recommendations":[{"id":8,"slug":"s2","name":"R","brand":"B","similarity":0.912345,
                "similarity_pct":91.23,"accords":[]}]}""",
            )
        }

        val result = api.recommendations(7, limit = 10, gender = Gender.Female)

        val url = requests.single().url
        assertEquals("/v1/perfumes/7/recommendations", url.encodedPath)
        assertEquals("female", url.parameters["gender"])
        assertEquals(91.23, result.recommendations.single().similarityPct)
    }

    @Test
    fun recommendationsOmitGenderWhenNull() = runTest {
        val api = api { json("""{"query":{"id":7,"slug":"s","name":"Q","brand":"B"},"recommendations":[]}""") }

        api.recommendations(7)

        assertNull(requests.single().url.parameters["gender"])
    }

    @Test
    fun problemJsonBecomesHttpException() = runTest {
        val api = api {
            json(
                """{"status":422,"code":"perfume_without_features","title":"Unprocessable Content",
                "detail":"sem accords","request_id":"abc123def"}""",
                status = HttpStatusCode.UnprocessableEntity,
                contentType = "application/problem+json",
            )
        }

        val error = assertFailsWith<ApiException.Http> { api.recommendations(3) }

        assertEquals(422, error.status)
        assertEquals(ProblemCode.PERFUME_WITHOUT_FEATURES, error.code)
        assertEquals("abc123def", error.requestId)
    }

    @Test
    fun nonProblemErrorBodyStillMapsToHttpException() = runTest {
        val api = api {
            respond(
                "<html>Bad Gateway</html>",
                HttpStatusCode.BadGateway,
                headersOf(HttpHeaders.ContentType, "text/html"),
            )
        }

        val error = assertFailsWith<ApiException.Http> { api.perfume(1) }

        assertEquals(502, error.status)
        assertEquals("http_502", error.code)
    }

    @Test
    fun connectionFailureBecomesNetworkException() = runTest {
        val api = api { throw RuntimeException("connection refused") }

        assertFailsWith<ApiException.Network> { api.perfume(1) }
    }

    @Test
    fun malformedSuccessBodyBecomesUnexpectedException() = runTest {
        val api = api { json("""{"nope":true}""") }

        assertFailsWith<ApiException.Unexpected> { api.perfume(1) }
    }
}
