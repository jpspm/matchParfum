package com.matchparfum.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matchparfum.data.model.PerfumeSummary
import com.matchparfum.ui.common.ErrorState
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.MessageState
import com.matchparfum.ui.common.PerfumeThumbnail
import com.matchparfum.ui.common.RatingLabel
import com.matchparfum.ui.common.metaLine
import com.matchparfum.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onPerfumeClick: (PerfumeSummary) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MatchParfum", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Find perfumes that smell alike",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Brand or perfume name") },
                leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(AppIcons.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            )

            Box(Modifier.fillMaxSize()) {
                when (val results = state.results) {
                    null -> HomeContent(
                        filter = state.filter,
                        onFilterChange = viewModel::onFilterChange,
                        recent = recent,
                        popular = state.popular,
                        onRetryPopular = viewModel::loadPopular,
                        onPerfumeClick = onPerfumeClick,
                    )
                    LoadState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    is LoadState.Failure -> ErrorState(results.error, onRetry = viewModel::retry)
                    is LoadState.Success -> if (results.data.isEmpty()) {
                        MessageState(
                            title = "No perfumes found",
                            message = "Nothing matches \"${state.query.trim()}\". Try another spelling.",
                        )
                    } else {
                        SearchResults(results.data, onPerfumeClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(items: List<PerfumeSummary>, onClick: (PerfumeSummary) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        items(items, key = { it.id }) { perfume ->
            PerfumeRow(perfume, onClick = { onClick(perfume) })
            HorizontalDivider(
                Modifier.padding(start = PERFUME_ROW_INSET),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
internal fun PerfumeRow(perfume: PerfumeSummary, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PerfumeThumbnail(perfume.id, perfume.brand)
        Column(Modifier.weight(1f)) {
            Text(
                perfume.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(perfume.brand, metaLine(perfume.year, perfume.gender))
                    .filter { it.isNotEmpty() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            RatingLabel(perfume.ratingAvg, perfume.voteCount)
        }
    }
}

/** Where list dividers start: past the row's thumbnail (16dp padding + 48dp image + 16dp gap). */
internal val PERFUME_ROW_INSET = 80.dp
