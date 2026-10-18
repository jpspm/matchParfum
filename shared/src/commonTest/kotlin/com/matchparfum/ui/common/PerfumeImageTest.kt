package com.matchparfum.ui.common

import kotlin.test.Test
import kotlin.test.assertEquals

class PerfumeImageTest {

    @Test
    fun lightThemeUsesJpg() {
        assertEquals(
            "https://fimgs.net/mdimg/perfume-thumbs/375x500.29927.jpg",
            perfumeImageUrl(29927, dark = false),
        )
    }

    @Test
    fun darkThemeUsesDarkWebp() {
        assertEquals(
            "https://fimgs.net/mdimg/perfume-thumbs/dark-375x500.29927.webp",
            perfumeImageUrl(29927, dark = true),
        )
    }

    @Test
    fun highResAddsDensitySuffix() {
        assertEquals(
            "https://fimgs.net/mdimg/perfume-thumbs/375x500.29927.2x.jpg",
            perfumeImageUrl(29927, dark = false, highRes = true),
        )
        assertEquals(
            "https://fimgs.net/mdimg/perfume-thumbs/dark-375x500.29927.2x.webp",
            perfumeImageUrl(29927, dark = true, highRes = true),
        )
    }
}
