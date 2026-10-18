package com.matchparfum.data.remote

import com.matchparfum.data.model.ProblemDetails

/** Every failure coming out of [PerfumeApi]. UI code switches on the subtype / [ProblemCode]. */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The server answered with a problem+json body (or at least a non-2xx status). */
    class Http(
        val status: Int,
        val code: String,
        val detail: String?,
        val requestId: String?,
    ) : ApiException("HTTP $status $code") {
        constructor(problem: ProblemDetails) :
            this(problem.status, problem.code, problem.detail, problem.requestId)
    }

    /** Could not reach the server (offline, DNS, refused connection, timeout). */
    class Network(cause: Throwable) : ApiException("Network error: ${cause.message}", cause)

    /** The server answered 2xx but the body did not match the contract. */
    class Unexpected(cause: Throwable) : ApiException("Unexpected response: ${cause.message}", cause)
}

/** Stable `code` values documented in perfume-api's README. */
object ProblemCode {
    const val PERFUME_NOT_FOUND = "perfume_not_found"
    const val PERFUME_NAME_NOT_FOUND = "perfume_name_not_found"
    const val AMBIGUOUS_PERFUME_NAME = "ambiguous_perfume_name"
    const val PERFUME_WITHOUT_FEATURES = "perfume_without_features"
    const val VALIDATION_ERROR = "validation_error"
    const val DATABASE_UNAVAILABLE = "database_unavailable"
    const val DATABASE_TIMEOUT = "database_timeout"
    const val INTERNAL_ERROR = "internal_error"
}
