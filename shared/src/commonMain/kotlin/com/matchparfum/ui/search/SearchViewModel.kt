package com.matchparfum.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class SearchUiState(
    val query: String = "",
    /** null while the query is too short to search. */
    val results: LoadState<List<PerfumeSummary>>? = null,
)

class SearchViewModel(private val api: PerfumeApi) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        search(debounce = true)
    }

    fun retry() = search(debounce = false)

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

        /** Server rejects `q` longer than 100 characters. */
        const val MAX_QUERY_LENGTH = 100
    }
}
