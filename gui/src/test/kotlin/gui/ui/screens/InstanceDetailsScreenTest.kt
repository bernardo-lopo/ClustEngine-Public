package gui.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import core.ClusterEngineFactoryInterface
import core.domain.ClustEngineInstance
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test

class InstanceDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testInstance =
        ClustEngineInstance(
            id = "instance-abc-123",
            privateIpAddress = "10.0.0.99",
            publicDns = "ec2.test.aws.com",
            publicIp = "192.168.1.1",
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Screen displays correct localized labels and instance details`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        state.selectAndNavigateToInstance(testInstance)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                InstanceDetailsScreen(state)
            }
        }

        composeTestRule.onNodeWithText(
            text = state.strings.nodeManagementTitle,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).assertExists()

        composeTestRule.onNodeWithText(
            text = state.strings.instanceDetailsSection,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).assertExists()

        composeTestRule.onNodeWithText(testInstance.id, substring = true, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText(testInstance.publicIp, substring = true, useUnmergedTree = true).assertExists()

        composeTestRule.onNodeWithText(
            text = state.strings.startNodeBtn,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).assertExists()

        composeTestRule.onNodeWithText(
            text = state.strings.stopNodeBtn,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Clicking terminate opens confirmation dialog with correct dynamic text`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        state.selectAndNavigateToInstance(testInstance)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                InstanceDetailsScreen(state)
            }
        }

        composeTestRule.onNodeWithText(
            text = state.strings.terminateNodeBtn,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).performClick()

        composeTestRule.onNodeWithText(
            text = state.strings.terminateInstanceDialogTitle,
            ignoreCase = true,
            useUnmergedTree = true,
        ).assertExists()

        val expectedDesc = state.strings.terminateInstanceDialogDesc(testInstance.id)
        composeTestRule.onNodeWithText(expectedDesc, substring = true, useUnmergedTree = true).assertExists()

        composeTestRule.onNodeWithText(
            text = state.strings.terminateBtn,
            ignoreCase = true,
            useUnmergedTree = true,
        ).assertExists()
    }
}
