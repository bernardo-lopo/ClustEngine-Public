package gui.state

import gui.state.enums.AppScreen
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationManagerTest {
    @Test
    fun `Initial state is SavedClusters and Sidebar is visible`() {
        val nav = NavigationManager()

        assertEquals(AppScreen.SavedClusters, nav.currentScreen)
        assertTrue(nav.isSidebarVisible)
    }

    @Test
    fun `MapsTo changes the current screen`() {
        val nav = NavigationManager()

        nav.navigateTo(AppScreen.Setup)
        assertEquals(AppScreen.Setup, nav.currentScreen)

        nav.navigateTo(AppScreen.Dashboard)
        assertEquals(AppScreen.Dashboard, nav.currentScreen)
    }

    @Test
    fun `toggleSidebar flips the boolean state`() {
        val nav = NavigationManager()

        nav.toggleSidebar()
        assertFalse(nav.isSidebarVisible)

        nav.toggleSidebar()
        assertTrue(nav.isSidebarVisible)
    }
}
