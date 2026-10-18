package com.matchparfum.data.remote

import io.ktor.client.engine.HttpClientEngineFactory

/** OkHttp on Android, Darwin (NSURLSession) on iOS. */
expect fun httpEngine(): HttpClientEngineFactory<*>

/**
 * Where perfume-api is reachable during development:
 * the Android emulator sees the host machine as 10.0.2.2, the iOS simulator as localhost.
 */
expect val defaultBaseUrl: String
