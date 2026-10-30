package com.smartarrange.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartarrange.app.domain.GridConfig
import com.smartarrange.app.domain.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.smartArrangeDataStore by preferencesDataStore(name = "smart_arrange_preferences")

class PreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.smartArrangeDataStore

    val preferences: Flow<UserPreferences> = dataStore.data.map { values ->
        val overrides = values.asMap().mapNotNull { (key, value) ->
            if (key.name.startsWith(OVERRIDE_PREFIX) && value is String) {
                key.name.removePrefix(OVERRIDE_PREFIX) to value
            } else {
                null
            }
        }.toMap()
        val names = values.asMap().mapNotNull { (key, value) ->
            if (key.name.startsWith(NAME_PREFIX) && value is String) {
                key.name.removePrefix(NAME_PREFIX) to value
            } else {
                null
            }
        }.toMap()
        UserPreferences(
            categoryOverrides = overrides,
            categoryNames = names,
            grid = GridConfig(
                columns = values[GRID_COLUMNS] ?: 4,
                rows = values[GRID_ROWS] ?: 5,
            ),
            preferredCategories = values[PREFERRED_CATEGORIES]
                .orEmpty()
                .split(',')
                .filter(String::isNotBlank)
                .toSet(),
        )
    }

    suspend fun setCategoryOverride(packageName: String, categoryId: String) {
        dataStore.edit { it[stringPreferencesKey("$OVERRIDE_PREFIX$packageName")] = categoryId }
    }

    suspend fun renameCategory(categoryId: String, name: String) {
        dataStore.edit { values ->
            val key = stringPreferencesKey("$NAME_PREFIX$categoryId")
            if (name.isBlank()) values.remove(key) else values[key] = name.trim()
        }
    }

    suspend fun setGrid(grid: GridConfig) {
        dataStore.edit {
            it[GRID_COLUMNS] = grid.columns.coerceIn(3, 7)
            it[GRID_ROWS] = grid.rows.coerceIn(3, 8)
        }
    }

    suspend fun setPreferredCategories(categoryIds: Set<String>) {
        dataStore.edit { it[PREFERRED_CATEGORIES] = categoryIds.sorted().joinToString(",") }
    }

    private companion object {
        const val OVERRIDE_PREFIX = "category_override_"
        const val NAME_PREFIX = "category_name_"
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val GRID_ROWS = intPreferencesKey("grid_rows")
        val PREFERRED_CATEGORIES = stringPreferencesKey("preferred_categories")
    }
}
