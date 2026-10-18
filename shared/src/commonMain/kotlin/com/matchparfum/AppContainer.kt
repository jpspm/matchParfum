package com.matchparfum

import com.matchparfum.data.remote.PerfumeApi
import com.matchparfum.data.remote.createHttpClient
import com.matchparfum.data.remote.defaultBaseUrl
import io.ktor.client.HttpClient

/** Manual dependency graph. One instance lives for the whole app. */
class AppContainer(baseUrl: String = defaultBaseUrl) {
    private val httpClient: HttpClient = createHttpClient(baseUrl)
    val api: PerfumeApi = PerfumeApi(httpClient)
}
