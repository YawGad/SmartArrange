package com.smartarrange.app.domain

import java.util.Locale

object CategorizationRules {
    fun categoryFor(name: String, packageName: String): String {
        val text = "$name $packageName".lowercase(Locale.ROOT)
        return when {
            text.hasAny(
                "whatsapp", "telegram", "signal", "messenger", "phone", "dialer",
                "contacts", "message", "sms", "discord", "wechat",
            ) -> "communication"
            text.hasAny(
                "sheets", "docs", "drive", "calendar", "office", "word", "excel",
                "notion", "notes", "keep", "tasks", "gmail", "outlook",
            ) -> "productivity"
            text.hasAny(
                "bank", "wallet", "finance", "kwsp", "akaun", "paypal", "wise",
                "touch 'n go", "tng", "maybank", "cimb",
            ) -> "finance"
            text.hasAny("grab", "maps", "waze", "uber", "travel", "booking", "airasia", "transport") ->
                "travel"
            text.hasAny(
                "youtube", "netflix", "spotify", "music", "video", "games",
                "tiktok", "instagram", "gallery", "photos",
            ) -> "media"
            text.hasAny(
                "settings", "camera", "clock", "calculator", "files", "browser",
                "brave", "chrome", "market", "store", "backup", "tools",
            ) -> "tools"
            else -> "other"
        }
    }

    fun categoryFor(app: InstalledApp, overrides: Map<String, String>): String =
        overrides[app.packageName] ?: app.defaultCategoryId

    private fun String.hasAny(vararg terms: String): Boolean = terms.any(::contains)
}
