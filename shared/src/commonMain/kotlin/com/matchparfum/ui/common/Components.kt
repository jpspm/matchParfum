package com.matchparfum.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.matchparfum.data.model.Gender
import com.matchparfum.data.model.WeightedItem
import com.matchparfum.ui.theme.AppIcons
import com.matchparfum.ui.theme.accordColor
import com.matchparfum.ui.theme.contentColorOn

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    footnote: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
        if (footnote != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                footnote,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
fun ErrorState(error: UiError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    MessageState(
        title = error.title,
        message = error.message,
        modifier = modifier,
        icon = AppIcons.CloudOff,
        footnote = error.requestId?.let { "Request ID: $it" },
        action = if (error.canRetry) {
            { Button(onClick = onRetry) { Text("Try again") } }
        } else {
            null
        },
    )
}

/** "★ 4.12 · 1,234 votes", or nothing when the perfume was never rated. */
@Composable
fun RatingLabel(rating: Double?, votes: Int, modifier: Modifier = Modifier) {
    if (rating == null || votes == 0) return
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            AppIcons.Star,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            "${rating.format(2)} · ${votes.withThousands()} votes",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Joins year, gender and any extra parts with " · ", skipping missing ones. */
fun metaLine(year: Int?, gender: Gender?, vararg extra: String?): String =
    listOfNotNull(year?.toString(), gender?.label, *extra).joinToString(" · ")

@Composable
fun AccordChip(accord: String, modifier: Modifier = Modifier) {
    val color = accordColor(accord)
    Text(
        accord,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = contentColorOn(color),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccordChips(accords: List<WeightedItem>, max: Int, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        accords.take(max).forEach { AccordChip(it.name) }
    }
}

/** Fragrantica-style horizontal bars, width proportional to the accord's weight. */
@Composable
fun AccordBars(accords: List<WeightedItem>, modifier: Modifier = Modifier) {
    val maxWeight = accords.maxOfOrNull { it.weight }?.takeIf { it > 0 } ?: 100.0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        accords.forEach { accord ->
            val color = accordColor(accord.name)
            val fraction = (accord.weight / maxWeight).coerceIn(0.25, 1.0).toFloat()
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    accord.name,
                    color = contentColorOn(color),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}
