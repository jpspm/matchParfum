package com.matchparfum.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matchparfum.data.model.PerfumeDetail
import com.matchparfum.data.model.WeightedItem
import com.matchparfum.ui.common.AccordBars
import com.matchparfum.ui.common.ErrorState
import com.matchparfum.ui.common.LoadState
import com.matchparfum.ui.common.LoadingState
import com.matchparfum.ui.common.RatingLabel
import com.matchparfum.ui.common.SectionTitle
import com.matchparfum.ui.common.format
import com.matchparfum.ui.common.metaLine
import com.matchparfum.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onFindSimilar: (PerfumeDetail) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val perfume = (state as? LoadState.Success)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(perfume?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (val s = state) {
            LoadState.Loading -> LoadingState(modifier)
            is LoadState.Failure -> ErrorState(s.error, onRetry = viewModel::load, modifier = modifier)
            is LoadState.Success -> DetailContent(s.data, onFindSimilar = { onFindSimilar(s.data) }, modifier)
        }
    }
}

@Composable
private fun DetailContent(perfume: PerfumeDetail, onFindSimilar: () -> Unit, modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    val hasFeatures = perfume.accords.isNotEmpty() || perfume.notesTop.isNotEmpty() ||
        perfume.notesMiddle.isNotEmpty() || perfume.notesBase.isNotEmpty()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Header(perfume)

        Button(
            onClick = onFindSimilar,
            enabled = hasFeatures,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(AppIcons.Sparkle, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(if (hasFeatures) "Find similar perfumes" else "Not enough data to compare")
        }

        Performance(perfume)

        if (perfume.accords.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Main accords")
                AccordBars(perfume.accords)
            }
        }

        if (perfume.notesTop.isNotEmpty() || perfume.notesMiddle.isNotEmpty() || perfume.notesBase.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Notes")
                NotesTier("Top", perfume.notesTop)
                NotesTier("Heart", perfume.notesMiddle)
                NotesTier("Base", perfume.notesBase)
            }
        }

        perfume.url?.let { url ->
            TextButton(onClick = { uriHandler.openUri(url) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("View on Fragrantica")
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Icon(AppIcons.OpenInNew, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Header(perfume: PerfumeDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            perfume.brand.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(perfume.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        val meta = metaLine(perfume.year, perfume.gender, perfume.collection?.takeIf { it.isNotBlank() })
        if (meta.isNotEmpty()) {
            Text(meta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        RatingLabel(perfume.ratingAvg, perfume.voteCount)
    }
}

@Composable
private fun Performance(perfume: PerfumeDetail) {
    val metrics = listOfNotNull(
        perfume.longevityAvg?.let { Triple("Longevity", it, 5.0) },
        perfume.sillageAvg?.let { Triple("Sillage", it, 4.0) },
        perfume.priceValueAvg?.let { Triple("Value for money", it, 5.0) },
    )
    if (metrics.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.forEach { (label, value, max) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Text(
                            "${value.format(1)} / ${max.format(0)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (value / max).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        strokeCap = StrokeCap.Round,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        drawStopIndicator = {},
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesTier(label: String, notes: List<WeightedItem>) {
    if (notes.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            notes.forEach { note -> SuggestionChip(onClick = {}, label = { Text(note.name) }) }
        }
    }
}
