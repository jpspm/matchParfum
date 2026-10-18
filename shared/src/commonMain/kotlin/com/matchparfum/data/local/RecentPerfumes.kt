package com.matchparfum.data.local

import com.matchparfum.data.model.PerfumeDetail
import com.matchparfum.data.model.PerfumeSummary
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Perfumes the user opened most recently (newest first), persisted on the device. */
class RecentPerfumes(private val settings: Settings) {

    private val _items = MutableStateFlow(read())
    val items: StateFlow<List<PerfumeSummary>> = _items.asStateFlow()

    fun add(perfume: PerfumeSummary) {
        _items.update { current ->
            (listOf(perfume) + current.filterNot { it.id == perfume.id }).take(MAX_ITEMS)
        }
        settings.putString(KEY, json.encodeToString(_items.value))
    }

    private fun read(): List<PerfumeSummary> {
        val stored = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString(stored)
        } catch (_: SerializationException) {
            emptyList() // stored by an incompatible app version
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }

    companion object {
        const val MAX_ITEMS = 10
        private const val KEY = "recent_perfumes"
        private val json = Json { ignoreUnknownKeys = true }
    }
}

fun PerfumeDetail.toSummary() = PerfumeSummary(
    id = id,
    slug = slug,
    name = name,
    brand = brand,
    year = year,
    gender = gender,
    url = url,
    ratingAvg = ratingAvg,
    voteCount = voteCount,
)
