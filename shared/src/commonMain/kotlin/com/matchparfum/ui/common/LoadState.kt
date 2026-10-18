package com.matchparfum.ui.common

import com.matchparfum.data.remote.ApiException
import com.matchparfum.data.remote.ProblemCode
import kotlinx.coroutines.CancellationException

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Failure(val error: UiError) : LoadState<Nothing>
}

/** What the UI shows for a failure: a message, whether retrying makes sense, and a request id for support. */
data class UiError(
    val title: String,
    val message: String,
    val canRetry: Boolean,
    val requestId: String? = null,
)

suspend fun <T> loadCatching(block: suspend () -> T): LoadState<T> =
    try {
        LoadState.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        LoadState.Failure(e.toUiError())
    }

fun Throwable.toUiError(): UiError = when (this) {
    is ApiException.Network -> UiError(
        title = "Can't reach the server",
        message = "Check your connection and make sure perfume-api is running.",
        canRetry = true,
    )
    is ApiException.Http -> toUiError()
    is ApiException.Unexpected -> UiError(
        title = "Unexpected response",
        message = "The server sent data this app doesn't understand. Try updating the app.",
        canRetry = true,
    )
    else -> UiError(
        title = "Something went wrong",
        message = message ?: "Unknown error.",
        canRetry = true,
    )
}

private fun ApiException.Http.toUiError(): UiError = when (code) {
    ProblemCode.PERFUME_NOT_FOUND -> UiError(
        title = "Perfume not found",
        message = "This perfume is no longer in the catalog.",
        canRetry = false,
        requestId = requestId,
    )
    ProblemCode.PERFUME_WITHOUT_FEATURES -> UiError(
        title = "No similar perfumes",
        message = "This perfume has no accords or notes on record, so similarity can't be computed.",
        canRetry = false,
        requestId = requestId,
    )
    ProblemCode.VALIDATION_ERROR -> UiError(
        title = "Invalid search",
        message = "Check what you typed and try again.",
        canRetry = false,
        requestId = requestId,
    )
    ProblemCode.DATABASE_UNAVAILABLE, ProblemCode.DATABASE_TIMEOUT -> UiError(
        title = "Service busy",
        message = "The service is temporarily unavailable. Try again in a moment.",
        canRetry = true,
        requestId = requestId,
    )
    else -> UiError(
        title = "Server error ($status)",
        message = "Something went wrong on the server. Try again.",
        canRetry = status >= 500,
        requestId = requestId,
    )
}
