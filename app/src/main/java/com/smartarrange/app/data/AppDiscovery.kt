package com.smartarrange.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.smartarrange.app.domain.CategorizationRules
import com.smartarrange.app.domain.InstalledApp
import com.smartarrange.app.domain.LauncherInfo

class AppDiscovery(private val context: Context) {
    @Suppress("DEPRECATION")
    fun scan(): ScanResult {
        val packageManager = context.packageManager
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val home = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val launcherInfo = home?.let {
            val packageName = it.activityInfo?.packageName ?: it.resolvePackageName ?: "unknown"
            val label = it.loadLabel(packageManager)?.toString() ?: packageName
            LauncherInfo(
                name = if (packageName == "android") "$label (chooser / not resolved)" else label,
                packageName = packageName,
            )
        } ?: LauncherInfo("Unknown", "unknown")

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedApps: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            packageManager.queryIntentActivities(launcherIntent, 0)
        }
        val apps = resolvedApps.mapNotNull { resolved ->
            val activity = resolved.activityInfo ?: return@mapNotNull null
            val packageName = activity.packageName
            if (packageName == context.packageName) return@mapNotNull null
            val label = resolved.loadLabel(packageManager)?.toString()?.takeIf(String::isNotBlank)
                ?: packageName
            InstalledApp(
                packageName = packageName,
                activityName = activity.name,
                label = label,
                defaultCategoryId = CategorizationRules.categoryFor(label, packageName),
            )
        }.distinctBy(InstalledApp::packageName)
            .sortedWith(
                compareBy<InstalledApp, String>(String.CASE_INSENSITIVE_ORDER) { it.label }
                    .thenBy { it.packageName },
            )
        return ScanResult(launcherInfo, apps)
    }
}

data class ScanResult(
    val launcher: LauncherInfo,
    val apps: List<InstalledApp>,
)
