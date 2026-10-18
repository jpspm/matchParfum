package com.matchparfum.data.remote

import android.os.Build
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpEngine(): HttpClientEngineFactory<*> = OkHttp

/**
 * Emulator: the host machine is 10.0.2.2.
 * Physical device: localhost, forwarded to the host over USB with `adb reverse tcp:8000 tcp:8000`.
 */
actual val defaultBaseUrl: String =
    if (isEmulator()) "http://10.0.2.2:8000" else "http://localhost:8000"

private fun isEmulator(): Boolean =
    Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish" ||
        Build.PRODUCT.startsWith("sdk") || Build.FINGERPRINT.startsWith("generic")
