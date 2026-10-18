package com.matchparfum.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matchparfum.data.local.RecentPerfumes
import com.matchparfum.data.model.Gender
import com.matchparfum.data.model.PerfumeSummary
import com.matchparfum.data.remote.PerfumeApi
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.loadCatching
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Home filter chips; each one picks which popular list is shown. */
enum class PopularFilter(val label: String, val gender: Gender?) {
    All("Most popular", null),
    Men("Men", Gender.Male),
    Women("Women", Gender.Female),
    Unisex("Unisex", Gender.Unisex),
}

data class SearchUiState(
    val query: String = "",
    /** null while the query is too short to search; the home content is shown instead. */
    val results: LoadState<List<PerfumeSummary>>? = null,
    val filter: PopularFilter = PopularFilter.All,
    val popular: LoadState<List<PerfumeSummary>> = LoadState.Loading,
)

class SearchViewModel(
    private val api: PerfumeApi,
    recentPerfumes: RecentPerfumes,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    val recent: StateFlow<List<PerfumeSummary>> = recentPerfumes.items

    private var searchJob: Job? = null
    private var popularJob: Job? = null

    /** Popular lists already fetched, so switching chips back and forth doesn't flicker. */
    private val popularCache = mutableMapOf<PopularFilter, List<PerfumeSummary>>()

    init {
        loadPopular()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        search(debounce = true)
    }

    fun retry() = search(debounce = false)

    fun onFilterChange(filter: PopularFilter) {
        if (filter == _state.value.filter) return
        _state.update { it.copy(filter = filter) }
        loadPopular()
    }

    fun loadPopular() {
        popularJob?.cancel()
        val filter = _state.value.filter
        popularCache[filter]?.let { cached ->
            _state.update { it.copy(popular = LoadState.Success(cached)) }
            return
        }
        _state.update { it.copy(popular = LoadState.Loading) }
        popularJob = viewModelScope.launch {
            val result = loadCatching { api.popular(POPULAR_LIMIT, filter.gender).items }
            if (result is LoadState.Success) popularCache[filter] = result.data
            _state.update { it.copy(popular = result) }
        }
    }

    private fun search(debounce: Boolean) {
        searchJob?.cancel()
        val query = _state.value.query.trim()
        if (query.length < PerfumeApi.MIN_QUERY_LENGTH) {
            _state.update { it.copy(results = null) }
            return
        }
        searchJob = viewModelScope.launch {
            if (debounce) delay(DEBOUNCE_MS)
            _state.update { it.copy(results = LoadState.Loading) }
            val result = loadCatching { api.search(query.take(MAX_QUERY_LENGTH)).items }
            _state.update { it.copy(results = result) }
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 350L
        const val POPULAR_LIMIT = 20

        /** Server rejects `q` longer than 100 characters. */
        const val MAX_QUERY_LENGTH = 100
    }
}
