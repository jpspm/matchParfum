package com.matchparfum.ui.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.matchparfum.data.model.PerfumeSummary
import com.matchparfum.ui.common.ErrorState
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.PerfumeImage
import com.matchparfum.ui.common.SectionTitle

/** What the search screen shows before the user types a query. */
@Composable
internal fun HomeContent(
    filter: PopularFilter,
    onFilterChange: (PopularFilter) -> Unit,
    recent: List<PerfumeSummary>,
    popular: LoadState<List<PerfumeSummary>>,
    onRetryPopular: () -> Unit,
    onPerfumeClick: (PerfumeSummary) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item(key = "filters") {
            FilterChips(filter, onFilterChange)
        }

        if (recent.isNotEmpty()) {
            item(key = "recent") {
                SectionTitle("Recently viewed", Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp))
                RecentRow(recent.take(RECENT_SHOWN), onPerfumeClick)
            }
        }

        item(key = "popular-title") {
            SectionTitle(
                filter.sectionTitle,
                Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
            )
        }
        when (popular) {
            LoadState.Loading -> item(key = "popular-loading") {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LoadState.Failure -> item(key = "popular-error") {
                ErrorState(popular.error, onRetry = onRetryPopular)
            }
            is LoadState.Success -> items(popular.data, key = { "popular-${it.id}" }) { perfume ->
                PerfumeRow(perfume, onClick = { onPerfumeClick(perfume) })
                HorizontalDivider(
                    Modifier.padding(start = PERFUME_ROW_INSET),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun FilterChips(selected: PopularFilter, onSelect: (PopularFilter) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PopularFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

/** Always three equal slots, so one or two recent perfumes don't stretch across the row. */
@Composable
private fun RecentRow(perfumes: List<PerfumeSummary>, onClick: (PerfumeSummary) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(RECENT_SHOWN) { index ->
            val perfume = perfumes.getOrNull(index)
            if (perfume != null) {
                RecentCard(perfume, onClick = { onClick(perfume) }, Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecentCard(perfume: PerfumeSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        PerfumeImage(perfume.id, perfume.brand, Modifier.fillMaxWidth())
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                perfume.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                perfume.brand,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val PopularFilter.sectionTitle: String
    get() = when (this) {
        PopularFilter.All -> "Popular perfumes"
        PopularFilter.Men -> "Popular for men"
        PopularFilter.Women -> "Popular for women"
        PopularFilter.Unisex -> "Popular unisex"
    }

private const val RECENT_SHOWN = 3
