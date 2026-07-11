package gui.state

import gui.i18n.Language
import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.util.Locale

object SystemPreferences {
    const val DOCS_URL = "https://github.com/bernardo-lopo/ClustEngine-Public"

    fun getSystemLanguage(): Language {
        val osLanguage = Locale.getDefault().language
        return if (osLanguage.lowercase() == "pt") Language.PT else Language.EN
    }

    fun isLinuxDarkTheme(): Boolean {
        return try {
            val process = ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "color-scheme").start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()?.trim() ?: ""
            process.waitFor()
            output.contains("prefer-dark")
        } catch (_: Exception) {
            false
        }
    }

    fun openDocumentation(onError: (String) -> Unit) {
        try {
            Desktop.getDesktop().browse(URI(DOCS_URL))
        } catch (e: Exception) {
            onError(e.message ?: "Error opening documentation")
        }
    }
}
