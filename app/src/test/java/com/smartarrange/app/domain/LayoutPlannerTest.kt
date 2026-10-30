package com.smartarrange.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutPlannerTest {
    @Test
    fun gridCapacityUsesConfiguredRowsAndColumns() {
        assertEquals(24, GridConfig(columns = 6, rows = 4).capacity)
    }

    @Test
    fun layoutIsDeterministicAndRespectsGridCapacity() {
        val apps = listOf(
            app("Zeta", "zeta", "other"),
            app("Beta", "beta", "other"),
            app("Alpha", "alpha", "other"),
            app("Solo", "solo", "tools"),
        )
        val grid = GridConfig(columns = 1, rows = 1)

        val first = LayoutPlanner.plan(apps, grid, emptyMap())
        val second = LayoutPlanner.plan(apps.reversed(), grid, emptyMap())

        assertEquals(first, second)
        assertEquals(2, first.pages.size)
        assertTrue(first.pages.all { it.size <= grid.capacity })
    }

    @Test
    fun groupsMultipleAppsInCategoryIntoRenamedFolder() {
        val apps = listOf(
            app("Chat B", "chat.b", "communication"),
            app("Chat A", "chat.a", "communication"),
            app("Solo", "solo", "tools"),
        )

        val layout = LayoutPlanner.plan(
            apps,
            GridConfig(columns = 4, rows = 4),
            mapOf("communication" to "Friends"),
        )
        val entries = layout.pages.flatten()

        val folder = entries.filterIsInstance<PreviewEntry.Folder>().single()
        assertEquals("Friends", folder.title)
        assertEquals(listOf("Chat A", "Chat B"), folder.apps.map { it.label })
        assertEquals("Solo", (entries.single { it is PreviewEntry.Shortcut } as PreviewEntry.Shortcut).app.label)
    }

    @Test
    fun preferredCategoriesAppearBeforeOtherGroups() {
        val apps = listOf(app("Notes", "notes", "productivity"), app("Camera", "camera", "tools"))
        val entries = LayoutPlanner.plan(
            apps,
            GridConfig(columns = 4, rows = 4),
            emptyMap(),
            preferredCategories = setOf("tools"),
        ).pages.single()

        assertEquals("Camera", (entries.first() as PreviewEntry.Shortcut).app.label)
    }

    private fun app(label: String, packageName: String, categoryId: String) =
        InstalledApp(packageName, "$packageName.Main", label, categoryId)
}
