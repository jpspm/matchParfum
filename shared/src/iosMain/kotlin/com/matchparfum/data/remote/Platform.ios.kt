package com.matchparfum.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun httpEngine(): HttpClientEngineFactory<*> = Darwin

actual val defaultBaseUrl: String = "http://localhost:8000"
