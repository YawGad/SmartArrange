package com.smartarrange.app.domain

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val defaultCategoryId: String,
    val categoryId: String = defaultCategoryId,
)

data class LauncherInfo(
    val name: String,
    val packageName: String,
)

data class AppCategory(
    val id: String,
    val title: String,
)

object AppCategories {
    val all = listOf(
        AppCategory("communication", "Communication"),
        AppCategory("productivity", "Productivity"),
        AppCategory("finance", "Finance"),
        AppCategory("travel", "Travel & transport"),
        AppCategory("media", "Media & entertainment"),
        AppCategory("tools", "Tools & system"),
        AppCategory("other", "Other"),
    )

    fun titleFor(id: String): String = all.firstOrNull { it.id == id }?.title ?: id
}

data class GridConfig(
    val columns: Int = 4,
    val rows: Int = 5,
) {
    val capacity: Int get() = columns.coerceAtLeast(1) * rows.coerceAtLeast(1)
}

data class UserPreferences(
    val categoryOverrides: Map<String, String> = emptyMap(),
    val categoryNames: Map<String, String> = emptyMap(),
    val grid: GridConfig = GridConfig(),
    val preferredCategories: Set<String> = emptySet(),
)
