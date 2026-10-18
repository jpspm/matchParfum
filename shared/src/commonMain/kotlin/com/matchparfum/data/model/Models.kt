package com.matchparfum.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `app/schemas.py` from perfume-api. */

@Serializable
data class WeightedItem(
    val name: String,
    /** Relative intensity (0-100) as reported by Fragrantica. */
    val weight: Double,
)

@Serializable
enum class Gender(val label: String) {
    @SerialName("male") Male("Masculine"),
    @SerialName("female") Female("Feminine"),
    @SerialName("unisex") Unisex("Unisex");

    /** Value sent in the `gender` query parameter. */
    val apiValue: String
        get() = when (this) {
            Male -> "male"
            Female -> "female"
            Unisex -> "unisex"
        }
}

@Serializable
data class PerfumeSummary(
    val id: Long,
    val slug: String,
    val name: String,
    val brand: String,
    val year: Int? = null,
    val gender: Gender? = null,
    val url: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double? = null,
    @SerialName("vote_count") val voteCount: Int = 0,
)

@Serializable
data class PerfumeDetail(
    val id: Long,
    val slug: String,
    val name: String,
    val brand: String,
    val year: Int? = null,
    val gender: Gender? = null,
    val url: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double? = null,
    @SerialName("vote_count") val voteCount: Int = 0,
    val collection: String? = null,
    /** 1-5 scale. */
    @SerialName("longevity_avg") val longevityAvg: Double? = null,
    /** 1-4 scale. */
    @SerialName("sillage_avg") val sillageAvg: Double? = null,
    /** 1-5 scale. */
    @SerialName("price_value_avg") val priceValueAvg: Double? = null,
    val accords: List<WeightedItem> = emptyList(),
    @SerialName("notes_top") val notesTop: List<WeightedItem> = emptyList(),
    @SerialName("notes_middle") val notesMiddle: List<WeightedItem> = emptyList(),
    @SerialName("notes_base") val notesBase: List<WeightedItem> = emptyList(),
)

@Serializable
data class Recommendation(
    val id: Long,
    val slug: String,
    val name: String,
    val brand: String,
    val year: Int? = null,
    val gender: Gender? = null,
    val url: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double? = null,
    @SerialName("vote_count") val voteCount: Int = 0,
    val accords: List<WeightedItem> = emptyList(),
    /** Cosine similarity, 0 to 1. */
    val similarity: Double,
    @SerialName("similarity_pct") val similarityPct: Double,
)

@Serializable
data class RecommendationResponse(
    val query: PerfumeDetail,
    val recommendations: List<Recommendation>,
)

@Serializable
data class SearchResponse(
    val items: List<PerfumeSummary>,
)

/** Body of every error response (`application/problem+json`). */
@Serializable
data class ProblemDetails(
    val status: Int,
    val code: String,
    val title: String,
    val detail: String? = null,
    @SerialName("request_id") val requestId: String? = null,
)
