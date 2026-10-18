package com.matchparfum.ui.similar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matchparfum.data.model.Brand
import com.matchparfum.data.model.Gender
import com.matchparfum.data.model.Recommendation
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

data class SimilarUiState(
    /** null = any gender. */
    val gender: Gender? = null,
    /** Brand the results are restricted to; null = any brand. */
    val brand: String? = null,
    /** Text typed in the brand field (differs from [brand] while the user is still choosing). */
    val brandQuery: String = "",
    val brandSuggestions: List<Brand> = emptyList(),
    val results: LoadState<List<Recommendation>> = LoadState.Loading,
)

class SimilarViewModel(
    private val api: PerfumeApi,
    private val perfumeId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(SimilarUiState())
    val state: StateFlow<SimilarUiState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var suggestionsJob: Job? = null

    init {
        load()
    }

    fun onGenderChange(gender: Gender?) {
        if (gender == _state.value.gender) return
        _state.update { it.copy(gender = gender) }
        load()
    }

    fun onBrandQueryChange(query: String) {
        _state.update { it.copy(brandQuery = query) }
        suggestionsJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty() || trimmed.equals(_state.value.brand, ignoreCase = true)) {
            _state.update { it.copy(brandSuggestions = emptyList()) }
            return
        }
        suggestionsJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            // Suggestions are a convenience: on failure just show none.
            val result = loadCatching { api.brands(trimmed.take(MAX_BRAND_LENGTH), SUGGESTION_LIMIT).items }
            val suggestions = (result as? LoadState.Success)?.data.orEmpty()
            _state.update { it.copy(brandSuggestions = suggestions) }
        }
    }

    fun onBrandSelected(brand: Brand) {
        suggestionsJob?.cancel()
        _state.update { it.copy(brand = brand.name, brandQuery = brand.name, brandSuggestions = emptyList()) }
        load()
    }

    fun onBrandCleared() {
        suggestionsJob?.cancel()
        val hadBrand = _state.value.brand != null
        _state.update { it.copy(brand = null, brandQuery = "", brandSuggestions = emptyList()) }
        if (hadBrand) load()
    }

    fun load() {
        loadJob?.cancel()
        val (gender, brand) = _state.value.let { it.gender to it.brand }
        _state.update { it.copy(results = LoadState.Loading) }
        loadJob = viewModelScope.launch {
            val result = loadCatching {
                api.recommendations(perfumeId, LIMIT, gender, brand).recommendations
            }
            _state.update { it.copy(results = result) }
        }
    }

    private companion object {
        const val LIMIT = 30
        const val DEBOUNCE_MS = 300L
        const val SUGGESTION_LIMIT = 8

        /** Server rejects brand filters longer than 100 characters. */
        const val MAX_BRAND_LENGTH = 100
    }
}
