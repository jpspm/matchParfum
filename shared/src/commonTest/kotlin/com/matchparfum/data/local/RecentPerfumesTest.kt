package com.matchparfum.data.local

import com.matchparfum.data.model.PerfumeSummary
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecentPerfumesTest {

    private fun perfume(id: Long) = PerfumeSummary(id = id, slug = "s$id", name = "P$id", brand = "B")

    @Test
    fun newestFirstWithoutDuplicates() {
        val recent = RecentPerfumes(MapSettings())

        recent.add(perfume(1))
        recent.add(perfume(2))
        recent.add(perfume(1))

        assertEquals(listOf(1L, 2L), recent.items.value.map { it.id })
    }

    @Test
    fun keepsAtMostMaxItems() {
        val recent = RecentPerfumes(MapSettings())

        (1L..RecentPerfumes.MAX_ITEMS + 5L).forEach { recent.add(perfume(it)) }

        assertEquals(RecentPerfumes.MAX_ITEMS, recent.items.value.size)
        assertEquals(RecentPerfumes.MAX_ITEMS + 5L, recent.items.value.first().id)
    }

    @Test
    fun survivesRestart() {
        val settings = MapSettings()
        RecentPerfumes(settings).add(perfume(7))

        assertEquals(listOf(7L), RecentPerfumes(settings).items.value.map { it.id })
    }

    @Test
    fun ignoresCorruptStorage() {
        val settings = MapSettings("recent_perfumes" to "not json")

        assertTrue(RecentPerfumes(settings).items.value.isEmpty())
    }
}
