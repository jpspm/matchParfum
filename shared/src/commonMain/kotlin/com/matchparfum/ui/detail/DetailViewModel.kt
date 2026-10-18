package com.matchparfum.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matchparfum.data.local.RecentPerfumes
import com.matchparfum.data.local.toSummary
import com.matchparfum.data.model.PerfumeDetail
import com.matchparfum.data.remote.PerfumeApi
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.loadCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val api: PerfumeApi,
    private val recentPerfumes: RecentPerfumes,
    private val perfumeId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<PerfumeDetail>>(LoadState.Loading)
    val state: StateFlow<LoadState<PerfumeDetail>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            val result = loadCatching { api.perfume(perfumeId) }
            if (result is LoadState.Success) recentPerfumes.add(result.data.toSummary())
            _state.value = result
        }
    }
}
