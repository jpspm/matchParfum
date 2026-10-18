package com.matchparfum.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Colors loosely inspired by Fragrantica's accord bars. Unknown accords get a stable hashed hue. */
private val accordColors = mapOf(
    "citrus" to 0xFFE8D53A,
    "fresh spicy" to 0xFF8FC744,
    "warm spicy" to 0xFFCC3300,
    "soft spicy" to 0xFFE28E5C,
    "aromatic" to 0xFF37A089,
    "green" to 0xFF0E8C1D,
    "herbal" to 0xFF6D9F49,
    "woody" to 0xFF774414,
    "sweet" to 0xFFEE363B,
    "fruity" to 0xFFFC4B29,
    "tropical" to 0xFFFFB533,
    "floral" to 0xFFFF5F8D,
    "white floral" to 0xFFEDD9BF,
    "yellow floral" to 0xFFFFD447,
    "rose" to 0xFFFE016B,
    "iris" to 0xFFB8A1D9,
    "violet" to 0xFF8F4FCD,
    "lavender" to 0xFFA688D0,
    "tuberose" to 0xFFF6E7D2,
    "powdery" to 0xFFEEDDCC,
    "musky" to 0xFFE2D4E8,
    "amber" to 0xFFBC4D10,
    "vanilla" to 0xFFFFF1B5,
    "balsamic" to 0xFF9B6A42,
    "fresh" to 0xFF9BE5ED,
    "aquatic" to 0xFF2FA8D4,
    "marine" to 0xFF1F7CA8,
    "ozonic" to 0xFF8FD1F0,
    "salty" to 0xFFCFE8EC,
    "mineral" to 0xFF9AA5A8,
    "metallic" to 0xFFA8B2B8,
    "earthy" to 0xFF544838,
    "mossy" to 0xFF5F7D3A,
    "patchouli" to 0xFF6B4F2E,
    "leather" to 0xFF7A4A2D,
    "animalic" to 0xFF8A5A3B,
    "smoky" to 0xFF8D8D8D,
    "tobacco" to 0xFFC79B5B,
    "oud" to 0xFF4E3524,
    "honey" to 0xFFF3B13B,
    "beeswax" to 0xFFE8B04A,
    "caramel" to 0xFFC27A2A,
    "chocolate" to 0xFF4B2A19,
    "coffee" to 0xFF5B3A1F,
    "cinnamon" to 0xFFB5651D,
    "nutty" to 0xFFA0682E,
    "almond" to 0xFFE9C8A2,
    "coconut" to 0xFFF4ECDC,
    "lactonic" to 0xFFFFF5E1,
    "cherry" to 0xFFB2003B,
    "rum" to 0xFF8C4A2F,
    "whiskey" to 0xFFB3742D,
    "wine" to 0xFF722F37,
    "anis" to 0xFFC6D7A7,
    "camphor" to 0xFFCFE3D8,
    "conifer" to 0xFF2E5D3E,
    "terpenic" to 0xFF5F8F5A,
    "sour" to 0xFFD7E35A,
    "cannabis" to 0xFF4C7A34,
)

fun accordColor(accord: String): Color {
    accordColors[accord.lowercase()]?.let { return Color(it) }
    val hue = (accord.lowercase().hashCode() and 0x7FFFFFFF) % 360
    return Color.hsl(hue.toFloat(), saturation = 0.45f, lightness = 0.6f)
}

fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.45f) Color(0xFF1F1A1C) else Color.White
