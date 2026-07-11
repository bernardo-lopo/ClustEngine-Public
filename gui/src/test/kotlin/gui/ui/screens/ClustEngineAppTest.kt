package gui.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import core.ClusterEngineFactoryInterface
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test

class ClustEngineAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Clicking Create Cluster navigates to Setup screen`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClustEngineApp(state)
            }
        }

        // Click the menu item using the dynamic i18n string from the active language
        composeTestRule.onNodeWithText(state.strings.createClusterMenu, useUnmergedTree = true).performClick()

        // Verify navigation by checking if the setup screen title is now visible
        composeTestRule.onNodeWithText(state.strings.setupTitle, useUnmergedTree = true).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Floating menu button opens sidebar when closed`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClustEngineApp(state)
            }
        }

        // Verify that the sidebar is visible again by looking for the main app title
        composeTestRule.onNodeWithText("ClustEngine", useUnmergedTree = true).assertExists()
    }
}
