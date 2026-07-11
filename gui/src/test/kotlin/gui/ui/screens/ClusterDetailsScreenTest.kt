package gui.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import core.ClusterEngineFactoryInterface
import core.domain.ClustEngineInstance
import core.domain.LiveCluster
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test

class ClusterDetailsScreenTest {
    private val testPrimaryInstance =
        ClustEngineInstance(
            id = "1",
            privateIpAddress = "10.0.0.1",
            publicDns = "primary.public.dns",
            publicIp = "123.123.123.1",
        )

    private val testSecondaryInstances =
        listOf(
            ClustEngineInstance(
                id = "2",
                privateIpAddress = "10.0.0.2",
                publicDns = "node2.public.dns",
                publicIp = "123.123.123.2",
            ),
            ClustEngineInstance(
                id = "3",
                privateIpAddress = "10.0.0.3",
                publicDns = "node3.public.dns",
                publicIp = "123.123.123.3",
            ),
        )

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Cluster details displays active cluster information and action buttons`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        val mockCluster =
            LiveCluster(
                clusterName = "Prod-DB",
                clusterSize = 3,
                provider = "AWS",
                clusterId = "123",
                primaryInstance = testPrimaryInstance,
                instances = testSecondaryInstances,
                clusterKeyFilePath = "/path/to/file",
                instanceTypeId = "t2.micro",
            )

        state.selectAndNavigateToDashboard(mockCluster)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClusterDetailsScreen(state = state)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Prod-DB", substring = true, useUnmergedTree = true).assertExists()

        // Use the translated strings dynamically
        composeTestRule.onNodeWithText("${state.strings.providerLabel}: AWS", substring = true, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("${state.strings.instancesLabel}: 3", substring = true, useUnmergedTree = true).assertExists()

        // Assert global action buttons are visible
        composeTestRule.onNodeWithText(state.strings.startBtn, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText(state.strings.stopBtn, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText(state.strings.terminateBtn, useUnmergedTree = true).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Cluster details displays primary and secondary instances details`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        val mockCluster =
            LiveCluster(
                clusterName = "Prod-DB",
                clusterSize = 3,
                provider = "AWS",
                clusterId = "123",
                primaryInstance = testPrimaryInstance,
                instances = testSecondaryInstances,
                clusterKeyFilePath = "/path/to/file",
                instanceTypeId = "t2.micro",
            )
        state.selectAndNavigateToDashboard(mockCluster)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClusterDetailsScreen(state)
            }
        }

        // Verify Primary Instance details are rendered (Using substring for flexibility)
        composeTestRule.onNodeWithText(testPrimaryInstance.publicIp, substring = true, useUnmergedTree = true).assertExists()

        // Verify Secondary Instances are rendered
        composeTestRule.onNodeWithText(testSecondaryInstances[0].publicIp, substring = true, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText(testSecondaryInstances[1].publicIp, substring = true, useUnmergedTree = true).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Cluster details handles cluster with zero active instances gracefully`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        val emptyCluster =
            LiveCluster(
                clusterName = "Starting-Cluster",
                clusterSize = 0,
                provider = "OpenStack",
                clusterId = "456",
                primaryInstance = testPrimaryInstance,
                instances = emptyList(),
                clusterKeyFilePath = "/path/to/file",
                instanceTypeId = "t2.micro",
            )
        state.selectAndNavigateToDashboard(emptyCluster)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClusterDetailsScreen(state)
            }
        }

        composeTestRule.onNodeWithText("Starting-Cluster", substring = true, useUnmergedTree = true).assertExists()

        // Assert using the translation dictionary
        composeTestRule.onNodeWithText("${state.strings.instancesLabel}: 0", useUnmergedTree = true).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Clicking Terminate opens confirmation dialog`() {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)

        val state =
            ClustEngineState(
                coroutineScope = TestScope(UnconfinedTestDispatcher()),
                clusterEngineFactory = mockFactory,
            )

        val mockCluster =
            LiveCluster(
                clusterName = "To-Be-Deleted",
                clusterSize = 1,
                provider = "AWS",
                clusterId = "999",
                primaryInstance = testPrimaryInstance,
                instances = emptyList(),
                clusterKeyFilePath = "/path/to/file",
                instanceTypeId = "t2.micro",
            )
        state.selectAndNavigateToDashboard(mockCluster)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                ClusterDetailsScreen(state)
            }
        }

        // Ensure the dialog is NOT visible initially
        composeTestRule.onNodeWithText(state.strings.confirmDeleteClusterTitle, useUnmergedTree = true).assertDoesNotExist()

        // Perform the click on the dynamic Terminate button string
        composeTestRule.onNodeWithText(state.strings.terminateBtn, substring = true, useUnmergedTree = true).performClick()

        // Assert the dialog title appears
        composeTestRule.onNodeWithText(state.strings.confirmDeleteClusterTitle, useUnmergedTree = true).assertExists()

        // Assert dialog action buttons appear
        composeTestRule.onNodeWithText(state.strings.confirmBtn, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText(state.strings.cancelBtn, useUnmergedTree = true).assertExists()
    }
}
