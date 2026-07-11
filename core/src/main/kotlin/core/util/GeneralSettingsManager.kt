package core.util

import java.io.File

object GeneralSettingsManager {
    private val settingsFile = File(".clustenginesettings")

    fun saveSettings(
        theme: String,
        language: String,
    ) {
        val content =
            """
            THEME=$theme
            LANGUAGE=$language
            """.trimIndent()

        settingsFile.writeText(content)
    }

    fun loadTheme(): String? {
        if (!settingsFile.exists()) return null
        return settingsFile.readLines().find { it.startsWith("THEME=") }?.substringAfter("=")
    }

    fun loadLanguage(): String? {
        if (!settingsFile.exists()) return null
        return settingsFile.readLines().find { it.startsWith("LANGUAGE=") }?.substringAfter("=")
    }
}
