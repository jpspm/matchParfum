package com.matchparfum.ui.similar

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.matchparfum.data.model.Brand
import com.matchparfum.ui.common.withThousands
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matchparfum.data.model.Gender
import com.matchparfum.data.model.Recommendation
import com.matchparfum.ui.common.AccordChips
import com.matchparfum.ui.common.ErrorState
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.LoadingState
import com.matchparfum.ui.common.MessageState
import com.matchparfum.ui.common.PerfumeThumbnail
import com.matchparfum.ui.common.RatingLabel
import com.matchparfum.ui.common.format
import com.matchparfum.ui.common.metaLine
import com.matchparfum.ui.common.rememberEnterTransitionSettled
import com.matchparfum.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarScreen(
    viewModel: SimilarViewModel,
    perfumeName: String,
    lifecycle: Lifecycle,
    onBack: () -> Unit,
    onPerfumeClick: (Recommendation) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Keep the spinner during the enter slide; the list is composed once it has settled.
    val settled = rememberEnterTransitionSettled(lifecycle)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Similar to",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(perfumeName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            GenderFilter(state.gender, viewModel::onGenderChange)
            BrandFilter(
                query = state.brandQuery,
                selected = state.brand,
                suggestions = state.brandSuggestions,
                onQueryChange = viewModel::onBrandQueryChange,
                onSelect = viewModel::onBrandSelected,
                onClear = viewModel::onBrandCleared,
            )
            Box(Modifier.fillMaxSize()) {
                when (val results = state.results) {
                    LoadState.Loading -> LoadingState()
                    is LoadState.Success if !settled -> LoadingState()
                    is LoadState.Failure -> ErrorState(results.error, onRetry = viewModel::load)
                    is LoadState.Success -> if (results.data.isEmpty()) {
                        MessageState(
                            title = "No matches",
                            message = state.brand
                                ?.let { "No $it perfumes match these filters. Try another brand or gender." }
                                ?: "No similar perfumes for this filter. Try another one.",
                        )
                    } else {
                        RecommendationList(results.data, onPerfumeClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderFilter(selected: Gender?, onSelect: (Gender?) -> Unit) {
    val options = listOf<Gender?>(null) + Gender.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { gender ->
            FilterChip(
                selected = gender == selected,
                onClick = { onSelect(gender) },
                label = { Text(gender?.label ?: "All") },
            )
        }
    }
}

/** Brand search with suggestions; results are filtered only once a suggestion is picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandFilter(
    query: String,
    selected: String?,
    suggestions: List<Brand>,
    onQueryChange: (String) -> Unit,
    onSelect: (Brand) -> Unit,
    onClear: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var menuOpen by remember(suggestions) { mutableStateOf(true) }
    val expanded = menuOpen && suggestions.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { menuOpen = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            placeholder = { Text("Filter by brand") },
            leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty() || selected != null) {
                    IconButton(onClick = {
                        onClear()
                        focusManager.clearFocus()
                    }) {
                        Icon(AppIcons.Close, contentDescription = "Clear brand")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { menuOpen = false }) {
            suggestions.forEach { brand ->
                DropdownMenuItem(
                    text = { Text(brand.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    trailingIcon = {
                        Text(
                            brand.perfumeCount.withThousands(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {
                        onSelect(brand)
                        focusManager.clearFocus()
                    },
                )
            }
        }
    }
}

@Composable
private fun RecommendationList(items: List<Recommendation>, onClick: (Recommendation) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
            RecommendationCard(rank = index + 1, item = item, onClick = { onClick(item) })
        }
    }
}

@Composable
private fun RecommendationCard(rank: Int, item: Recommendation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(40.dp),
                )
                PerfumeThumbnail(item.id, item.brand)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        listOf(item.brand, metaLine(item.year, item.gender))
                            .filter { it.isNotEmpty() }
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    RatingLabel(item.ratingAvg, item.voteCount)
                }
                Text(
                    "${item.similarityPct.format(0)}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { item.similarity.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                drawStopIndicator = {},
            )
            if (item.accords.isNotEmpty()) {
                AccordChips(item.accords, max = 4)
            }
        }
    }
}
