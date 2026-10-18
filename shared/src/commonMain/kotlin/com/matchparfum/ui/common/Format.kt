package com.matchparfum.ui.common

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/** `String.format` isn't available in common code. */
fun Double.format(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val scaled = (abs(this) * factor).roundToLong()
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    val whole = scaled / factor.toLong()
    if (decimals == 0) return "$sign$whole"
    val fraction = (scaled % factor.toLong()).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}

/** 12345 -> "12,345". */
fun Int.withThousands(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()
