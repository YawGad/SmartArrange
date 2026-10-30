package com.smartarrange.app.domain

sealed interface PreviewEntry {
    data class Shortcut(val app: InstalledApp) : PreviewEntry
    data class Folder(
        val categoryId: String,
        val title: String,
        val apps: List<InstalledApp>,
    ) : PreviewEntry
}

data class ProposedLayout(val pages: List<List<PreviewEntry>>)

object LayoutPlanner {
    fun plan(
        apps: List<InstalledApp>,
        grid: GridConfig,
        categoryNames: Map<String, String>,
        preferredCategories: Set<String> = emptySet(),
    ): ProposedLayout {
        val orderedCategories = AppCategories.all
            .sortedWith(
                compareByDescending<AppCategory> { it.id in preferredCategories }
                    .thenBy { AppCategories.all.indexOf(it) },
            )
        val entries = orderedCategories.flatMap { category ->
            val members = apps
                .filter { it.categoryId == category.id }
                .sortedWith(
                    compareBy<InstalledApp, String>(String.CASE_INSENSITIVE_ORDER) { it.label }
                        .thenBy { it.packageName },
                )
            when {
                members.size > 1 -> listOf(
                    PreviewEntry.Folder(
                        category.id,
                        categoryNames[category.id] ?: category.title,
                        members,
                    ),
                )
                members.size == 1 -> listOf(PreviewEntry.Shortcut(members.single()))
                else -> emptyList()
            }
        }
        val capacity = grid.capacity
        return ProposedLayout(
            entries.chunked(capacity).ifEmpty { listOf(emptyList()) },
        )
    }
}
