package com.matchparfum.ui.similar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matchparfum.data.model.Gender
import com.matchparfum.data.model.Recommendation
import com.matchparfum.data.remote.PerfumeApi
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.loadCatching
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimilarUiState(
    /** null = any gender. */
    val gender: Gender? = null,
    val results: LoadState<List<Recommendation>> = LoadState.Loading,
)

class SimilarViewModel(
    private val api: PerfumeApi,
    private val perfumeId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(SimilarUiState())
    val state: StateFlow<SimilarUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onGenderChange(gender: Gender?) {
        if (gender == _state.value.gender) return
        _state.update { it.copy(gender = gender) }
        load()
    }

    fun load() {
        loadJob?.cancel()
        val gender = _state.value.gender
        _state.update { it.copy(results = LoadState.Loading) }
        loadJob = viewModelScope.launch {
            val result = loadCatching { api.recommendations(perfumeId, LIMIT, gender).recommendations }
            _state.update { it.copy(results = result) }
        }
    }

    private companion object {
        const val LIMIT = 30
    }
}
