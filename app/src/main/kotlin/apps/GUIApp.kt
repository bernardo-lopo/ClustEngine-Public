package apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.state.enums.ThemeMode
import gui.ui.screens.ClustEngineApp
import gui.ui.theme.ClustEngineTheme

fun main() =
    application {
        val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "ClustEngine",
            state = windowState,
        ) {
            val coroutineScope = rememberCoroutineScope()
            val state = remember { ClustEngineState(coroutineScope, ClusterEngineFactoryLoader) }

            val useDarkMode by remember(state.themeMode) {
                derivedStateOf {
                    when (state.themeMode) {
                        ThemeMode.Light -> false
                        ThemeMode.Dark -> true
                        ThemeMode.Auto -> state.isDarkTheme
                    }
                }
            }

            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClustEngineTheme(isDarkMode = useDarkMode) {
                    ClustEngineApp(state)
                }
            }
        }
    }
