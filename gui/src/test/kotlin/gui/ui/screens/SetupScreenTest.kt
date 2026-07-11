package gui.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import core.ClusterEngineFactoryInterface
import core.domain.ClustEngineInstanceType
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test

class SetupScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Init button is disabled when form is empty`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SetupScreen(state)
            }
        }

        // Find the button by its dynamic translated text and assert it is not enabled
        composeTestRule.onNodeWithText(state.strings.initEngineBtn, ignoreCase = true)
            .assertIsNotEnabled()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Init button becomes enabled when form is filled`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SetupScreen(state)
            }
        }

        // Fill in Cluster Name
        composeTestRule.onNodeWithText("Cluster Name")
            .performTextInput("TestCluster")

        // Select predefined size chip
        composeTestRule.onNodeWithText("4").performClick()

        // Select Network Routing Mode
        composeTestRule.onNodeWithText("Single IP").performClick()

        // Select Cloud Provider
        composeTestRule.onNodeWithText("AWS").performClick()

        state.selectedFlavor = ClustEngineInstanceType("test-id", "Test Flavor", "Specs")

        // Verify state updated
        assert(state.clusterName == "TestCluster")
        assert(state.clusterSize == "4")

        // Verify button is now enabled using the correct translation string
        composeTestRule.onNodeWithText(state.strings.initEngineBtn, ignoreCase = true)
            .assertIsEnabled()
    }
}
