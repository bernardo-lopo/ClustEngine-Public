package gui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import gui.state.enums.AppScreen

class NavigationManager {
    var currentScreen by mutableStateOf(AppScreen.SavedClusters)
        private set

    var isSidebarVisible by mutableStateOf(true)
        private set

    fun navigateTo(screen: AppScreen) {
        if (currentScreen != screen) currentScreen = screen
    }

    fun toggleSidebar() {
        isSidebarVisible = !isSidebarVisible
    }
}
