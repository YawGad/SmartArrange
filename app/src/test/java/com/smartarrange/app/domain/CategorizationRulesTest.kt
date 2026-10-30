package com.smartarrange.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CategorizationRulesTest {
    @Test
    fun categorizesAppsUsingNameAndPackageRules() {
        assertEquals("communication", CategorizationRules.categoryFor("Signal", "org.signal"))
        assertEquals("productivity", CategorizationRules.categoryFor("Docs", "com.example.docs"))
        assertEquals("finance", CategorizationRules.categoryFor("My Bank", "com.example.bank"))
        assertEquals("travel", CategorizationRules.categoryFor("Waze", "com.waze"))
        assertEquals("media", CategorizationRules.categoryFor("Spotify", "com.spotify.music"))
        assertEquals("tools", CategorizationRules.categoryFor("Calculator", "com.example.calc"))
        assertEquals("other", CategorizationRules.categoryFor("Unknown", "com.example.unknown"))
    }

    @Test
    fun categoryOverrideTakesPrecedenceOverAutomaticRule() {
        val app = app("Signal", "org.signal", "communication")
        assertEquals("finance", CategorizationRules.categoryFor(app, mapOf("org.signal" to "finance")))
        assertEquals("communication", CategorizationRules.categoryFor(app, emptyMap()))
    }

    private fun app(label: String, packageName: String, categoryId: String) =
        InstalledApp(packageName, "$packageName.Main", label, categoryId)
}
