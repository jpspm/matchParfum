package com.matchparfum.ui.common

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    @Test
    fun formatsDecimals() {
        assertEquals("4.12", 4.1234.format(2))
        assertEquals("4.10", 4.1.format(2))
        assertEquals("92", 91.6.format(0))
        assertEquals("0.05", 0.049.format(2))
        assertEquals("-1.5", (-1.46).format(1))
    }

    @Test
    fun groupsThousands() {
        assertEquals("0", 0.withThousands())
        assertEquals("999", 999.withThousands())
        assertEquals("1,000", 1000.withThousands())
        assertEquals("1,234,567", 1234567.withThousands())
    }
}
