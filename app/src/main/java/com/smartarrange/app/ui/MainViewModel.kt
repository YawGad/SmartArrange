package com.smartarrange.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartarrange.app.data.AppDiscovery
import com.smartarrange.app.data.PreferencesRepository
import com.smartarrange.app.domain.GridConfig
import com.smartarrange.app.domain.InstalledApp
import com.smartarrange.app.domain.LauncherInfo
import com.smartarrange.app.domain.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val apps: List<InstalledApp> = emptyList(),
    val launcher: LauncherInfo = LauncherInfo("Checking launcher…", ""),
    val preferences: UserPreferences = UserPreferences(),
    val isScanning: Boolean = false,
    val hasScanned: Boolean = false,
    val lastScanMillis: Long? = null,
    val errorMessage: String? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)
    private val discovery = AppDiscovery(application)
    private val mutableState = MutableStateFlow(MainUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                preferencesRepository.preferences.collect { preferences ->
                    mutableState.update { current ->
                        current.copy(
                            preferences = preferences,
                            apps = current.apps.map { app ->
                                app.copy(
                                    categoryId = preferences.categoryOverrides[app.packageName]
                                        ?: app.defaultCategoryId,
                                )
                            },
                        )
                    }
                }
            } catch (error: Exception) {
                mutableState.update {
                    it.copy(errorMessage = "Could not load saved settings: ${error.message ?: "unknown error"}")
                }
            }
        }
        scanApps()
    }

    fun scanApps() {
        if (mutableState.value.isScanning) return
        viewModelScope.launch {
            mutableState.update { it.copy(isScanning = true, errorMessage = null) }
            try {
                val result = withContext(Dispatchers.IO) { discovery.scan() }
                val preferences = mutableState.value.preferences
                mutableState.update {
                    it.copy(
                        apps = result.apps.map { app ->
                            app.copy(
                                categoryId = preferences.categoryOverrides[app.packageName]
                                    ?: app.defaultCategoryId,
                            )
                        },
                        launcher = result.launcher,
                        isScanning = false,
                        hasScanned = true,
                        lastScanMillis = System.currentTimeMillis(),
                    )
                }
            } catch (error: Exception) {
                mutableState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = "App scan failed: ${error.message ?: error.javaClass.simpleName}",
                    )
                }
            }
        }
    }

    fun moveApp(app: InstalledApp, categoryId: String) {
        persistChange {
            preferencesRepository.setCategoryOverride(app.packageName, categoryId)
        }
    }

    fun renameCategory(categoryId: String, name: String) {
        persistChange { preferencesRepository.renameCategory(categoryId, name) }
    }

    fun setGrid(grid: GridConfig) {
        persistChange { preferencesRepository.setGrid(grid) }
    }

    fun togglePreferredCategory(categoryId: String) {
        val updated = state.value.preferences.preferredCategories.toMutableSet()
        if (!updated.add(categoryId)) updated.remove(categoryId)
        persistChange { preferencesRepository.setPreferredCategories(updated) }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    private fun persistChange(change: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                change()
            } catch (error: Exception) {
                mutableState.update {
                    it.copy(errorMessage = "Could not save your change: ${error.message ?: "unknown error"}")
                }
            }
        }
    }
}
