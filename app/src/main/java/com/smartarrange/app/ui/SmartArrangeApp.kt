package com.smartarrange.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartarrange.app.domain.AppCategories
import com.smartarrange.app.domain.AppCategory
import com.smartarrange.app.domain.GridConfig
import com.smartarrange.app.domain.InstalledApp
import com.smartarrange.app.domain.LayoutPlanner
import com.smartarrange.app.domain.PreviewEntry
import java.text.DateFormat
import java.util.Date

private enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Home),
    Analyze("Analyze", Icons.Outlined.Apps),
    Categories("Categories", Icons.Outlined.Category),
    Preview("Preview", Icons.Outlined.GridView),
    Settings("Settings", Icons.Outlined.Settings),
}

@Composable
fun SmartArrangeApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp),
            ) {
                Text(
                    "SmartArrange",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                )
                state.errorMessage?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(onClick = viewModel::clearError) { Text("Dismiss") }
                        }
                    }
                }
                when (selectedTab) {
                    AppTab.Home -> HomeScreen(state, viewModel::scanApps) { selectedTab = AppTab.Analyze }
                    AppTab.Analyze -> AnalyzeScreen(state, viewModel::scanApps)
                    AppTab.Categories -> CategoriesScreen(state, viewModel)
                    AppTab.Preview -> PreviewScreen(state)
                    AppTab.Settings -> SettingsScreen(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(state: MainUiState, scan: () -> Unit, openAnalyze: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("A calmer home screen starts with a plan.", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Discover your apps, choose your groups, and preview a layout. Your launcher stays in control.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = scan, enabled = !state.isScanning) {
                    androidx.compose.material3.Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isScanning) "Scanning…" else "Scan My Apps")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Apps", if (state.hasScanned) state.apps.size.toString() else "—", Modifier.weight(1f))
            MetricCard("Categories", AppCategories.all.size.toString(), Modifier.weight(1f))
        }
        val uncategorized = state.apps.count { it.categoryId == "other" }
        MetricCard("Other / review", uncategorized.toString(), Modifier.fillMaxWidth())
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Default launcher", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(state.launcher.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(state.launcher.packageName.ifBlank { "Checking device…" }, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Most recent scan", style = MaterialTheme.typography.labelLarge)
                Text(
                    if (state.isScanning) "Scanning installed apps…"
                    else state.lastScanMillis?.let { DateFormat.getDateTimeInstance().format(Date(it)) }
                        ?: "No scan yet",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        OutlinedButton(onClick = openAnalyze, modifier = Modifier.fillMaxWidth()) {
            Text("Review discovered apps")
        }
        Text(
            "SmartArrange creates a proposal only. It never replaces your launcher or moves icons.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnalyzeScreen(state: MainUiState, scan: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(state.apps, query) {
        state.apps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, true) }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Your installed apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "${state.apps.size} launchable apps · ${AppCategories.all.count { category -> state.apps.any { it.categoryId == category.id } }} categories in use",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search apps") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = scan, enabled = !state.isScanning, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.isScanning) "Scanning…" else "Scan again")
        }
        when {
            state.isScanning && !state.hasScanned -> LoadingState()
            !state.hasScanned -> EmptyState("Scan to discover launchable apps.")
            filtered.isEmpty() -> EmptyState(if (state.apps.isEmpty()) "No launchable apps were found." else "No apps match your search.")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(filtered, key = InstalledApp::packageName) { app ->
                    AppListRow(app)
                }
            }
        }
    }
}

@Composable
private fun CategoriesScreen(state: MainUiState, viewModel: MainViewModel) {
    var renameTarget by remember { mutableStateOf<AppCategory?>(null) }
    var renameValue by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Organize your way", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Changes are saved on this device and update your preview.")
        }
        items(AppCategories.all, key = AppCategory::id) { category ->
            val apps = state.apps.filter { it.categoryId == category.id }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                state.preferences.categoryNames[category.id] ?: category.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text("${apps.size} apps", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            renameValue = state.preferences.categoryNames[category.id] ?: category.title
                            renameTarget = category
                        }) { Text("Rename") }
                    }
                    if (apps.isEmpty()) {
                        Text("No apps yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        apps.forEach { app -> AppCategoryRow(app, viewModel::moveApp) }
                    }
                }
            }
        }
    }
    renameTarget?.let { category ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename category") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("Category name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameCategory(category.id, renameValue)
                    renameTarget = null
                }, enabled = renameValue.isNotBlank()) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AppCategoryRow(app: InstalledApp, moveApp: (InstalledApp, String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        AppIcon(app.packageName, Modifier.size(36.dp))
        Text(app.label, modifier = Modifier.weight(1f).padding(horizontal = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box {
            TextButton(onClick = { expanded = true }) { Text("Move") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AppCategories.all.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.title) },
                        onClick = {
                            moveApp(app, category.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewScreen(state: MainUiState) {
    val layout = remember(
        state.apps,
        state.preferences.grid,
        state.preferences.categoryNames,
        state.preferences.preferredCategories,
    ) {
        LayoutPlanner.plan(
            state.apps,
            state.preferences.grid,
            state.preferences.categoryNames,
            state.preferences.preferredCategories,
        )
    }
    var page by remember { mutableIntStateOf(0) }
    var openFolder by remember { mutableStateOf<PreviewEntry.Folder?>(null) }
    androidx.compose.runtime.LaunchedEffect(layout.pages.size) { page = 0 }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("A plan, not your launcher", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "Preview only — your home screen is unchanged. Folders are suggestions you can create in a supported launcher.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = { page = (page - 1).coerceAtLeast(0) }, enabled = page > 0) {
                androidx.compose.material3.Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous page")
            }
            Text("Page ${page + 1} of ${layout.pages.size}", modifier = Modifier.padding(horizontal = 12.dp))
            TextButton(
                onClick = { page = (page + 1).coerceAtMost(layout.pages.lastIndex) },
                enabled = page < layout.pages.lastIndex,
            ) {
                androidx.compose.material3.Icon(Icons.Outlined.ChevronRight, contentDescription = "Next page")
            }
        }
        if (!state.hasScanned) {
            EmptyState("Scan your apps to build a home-screen proposal.")
        } else if (state.apps.isEmpty()) {
            EmptyState("No launchable apps are available to preview.")
        } else {
            Card(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(state.preferences.grid.columns),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(layout.pages.getOrElse(page) { emptyList() }) { entry ->
                        PreviewCell(entry) { if (entry is PreviewEntry.Folder) openFolder = entry }
                    }
                }
            }
        }
    }
    openFolder?.let { folder ->
        AlertDialog(
            onDismissRequest = { openFolder = null },
            title = { Text(folder.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folder.apps.forEach { AppListRow(it) }
                }
            },
            confirmButton = { TextButton(onClick = { openFolder = null }) { Text("Done") } },
        )
    }
}

@Composable
private fun PreviewCell(entry: PreviewEntry, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (entry) {
            is PreviewEntry.Shortcut -> AppIcon(entry.app.packageName, Modifier.size(48.dp))
            is PreviewEntry.Folder -> {
                Box(
                    modifier = Modifier.size(58.dp).clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer).padding(5.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        entry.apps.take(4).chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                row.forEach { app -> AppIcon(app.packageName, Modifier.size(21.dp)) }
                            }
                        }
                    }
                    if (entry.apps.size > 4) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text("+${entry.apps.size - 4}", Modifier.padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            when (entry) {
                is PreviewEntry.Shortcut -> entry.app.label
                is PreviewEntry.Folder -> entry.title
            },
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsScreen(state: MainUiState, viewModel: MainViewModel) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Detected home app", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(state.launcher.name, style = MaterialTheme.typography.titleMedium)
                Text(state.launcher.packageName, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    },
                ) { Text("Open home-app settings") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Proposed grid", style = MaterialTheme.typography.titleMedium)
                GridSettingRow("Columns", state.preferences.grid.columns, 3..7) {
                    viewModel.setGrid(state.preferences.grid.copy(columns = it))
                }
                GridSettingRow("Rows", state.preferences.grid.rows, 3..8) {
                    viewModel.setGrid(state.preferences.grid.copy(rows = it))
                }
                Text(
                    "${state.preferences.grid.capacity} proposed slots per page",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Preferred categories", style = MaterialTheme.typography.titleMedium)
                Text("These groups appear first in your proposed layout.", style = MaterialTheme.typography.bodySmall)
                AppCategories.all.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = category.id in state.preferences.preferredCategories,
                            onCheckedChange = { viewModel.togglePreferredCategory(category.id) },
                        )
                        Text(state.preferences.categoryNames[category.id] ?: category.title)
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacy & launcher support", style = MaterialTheme.typography.titleMedium)
                Text("App names, icons and category choices are read and stored on this device.")
                Text("Discovery uses Android's visible launcher activities. Some work-profile, hidden or restricted apps may not appear.")
                Text("No internet, accessibility, root or device-administrator permission is requested.")
                Text("SmartArrange cannot write another app's home-screen layout. Create suggested folders and move icons yourself in a launcher that supports those actions.")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GridSettingRow(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        TextButton(onClick = { onChange((value - 1).coerceAtLeast(range.first)) }, enabled = value > range.first) {
            Text("−")
        }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { onChange((value + 1).coerceAtMost(range.last)) }, enabled = value < range.last) {
            Text("+")
        }
    }
}

@Composable
private fun AppListRow(app: InstalledApp) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(app.packageName, Modifier.size(42.dp))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                AppCategories.titleFor(app.categoryId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
            null
        }
    }
    if (bitmap != null) {
        Image(bitmap, contentDescription = "App icon", modifier = modifier.clip(RoundedCornerShape(12.dp)))
    } else {
        Box(
            modifier = modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(Icons.Outlined.Apps, contentDescription = "App icon")
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text("Finding launchable apps…")
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
