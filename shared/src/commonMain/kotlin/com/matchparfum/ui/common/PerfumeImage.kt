package com.matchparfum.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Bottle photo from Fragrantica's CDN. Perfume ids are Fragrantica ids, so the URL is derived
 * directly; there's a dark-background variant for dark mode. [highRes] picks the 750x1000 version.
 */
fun perfumeImageUrl(id: Long, dark: Boolean, highRes: Boolean = false): String {
    val density = if (highRes) ".2x" else ""
    return if (dark) {
        "https://fimgs.net/mdimg/perfume-thumbs/dark-375x500.$id$density.webp"
    } else {
        "https://fimgs.net/mdimg/perfume-thumbs/375x500.$id$density.jpg"
    }
}

/**
 * 3:4 bottle photo. The brand initial sits underneath, so it shows while loading and
 * whenever the CDN has no photo for this perfume.
 */
@Composable
fun PerfumeImage(
    id: Long,
    brand: String,
    modifier: Modifier = Modifier,
    highRes: Boolean = false,
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Box(
        modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            brand.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AsyncImage(
            model = perfumeImageUrl(id, dark, highRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** List-row sized [PerfumeImage]. */
@Composable
fun PerfumeThumbnail(id: Long, brand: String, modifier: Modifier = Modifier, width: Dp = 48.dp) {
    PerfumeImage(id, brand, modifier.size(width = width, height = width * 4 / 3))
}
