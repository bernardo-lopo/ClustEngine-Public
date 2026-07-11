package gui.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsEnabled
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SavedClusterScreenTest {
    private val testPrimaryInstance =
        ClustEngineInstance(
            id = "1",
            privateIpAddress = "10.0.0.1",
            publicDns = "primary.public.dns",
            publicIp = "123.123.123.1",
        )

    private val testSecondaryInstances =
        listOf(
            ClustEngineInstance(id = "2", privateIpAddress = "10.0.0.2", publicDns = "node2.public.dns", publicIp = "123.123.123.2"),
            ClustEngineInstance(id = "3", privateIpAddress = "10.0.0.3", publicDns = "node3.public.dns", publicIp = "123.123.123.3"),
        )

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createTestState(): ClustEngineState {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)
        return ClustEngineState(
            coroutineScope = TestScope(UnconfinedTestDispatcher()),
            clusterEngineFactory = mockFactory,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Empty state displays placeholder and create button`() {
        val state = createTestState()
        state.savedClusters = emptyList()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SavedClustersScreen(state = state)
            }
        }

        composeTestRule.onNodeWithText(
            text = state.strings.noClustersFoundTitle,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).assertExists()

        composeTestRule.onNodeWithText(
            text = state.strings.createNewClusterBtn,
            ignoreCase = true,
            substring = true,
            useUnmergedTree = true,
        ).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Populated state displays list of saved clusters`() {
        val state = createTestState()
        val mockClusters =
            listOf(
                LiveCluster(
                    clusterName = "Alpha-Cluster",
                    clusterSize = 3,
                    provider = "OpenStack",
                    clusterId = "mock-id-1",
                    primaryInstance = testPrimaryInstance,
                    instances = testSecondaryInstances,
                    clusterKeyFilePath = "/fake/path",
                    instanceTypeId = "t2.micro",
                ),
                LiveCluster(
                    clusterName = "Beta-Cluster",
                    clusterSize = 1,
                    provider = "AWS",
                    clusterId = "mock-id-2",
                    primaryInstance = testPrimaryInstance,
                    instances = emptyList(),
                    clusterKeyFilePath = "/fake/path",
                    instanceTypeId = "t2.micro",
                ),
            )

        state.savedClusters = mockClusters

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SavedClustersScreen(state)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Alpha-Cluster", substring = true, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Beta-Cluster", substring = true, useUnmergedTree = true).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Create New Cluster button is clickable and enabled`() {
        val state = createTestState()
        state.savedClusters = emptyList()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SavedClustersScreen(state)
            }
        }

        val createBtnNode =
            composeTestRule.onNodeWithText(
                text = state.strings.createNewClusterBtn,
                ignoreCase = true,
                substring = true,
                useUnmergedTree = true,
            )

        createBtnNode.assertExists()
        createBtnNode.assertIsEnabled()
        createBtnNode.performClick()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Clicking a cluster card updates the active cluster in state`() {
        val state = createTestState()
        val targetCluster =
            LiveCluster(
                clusterName = "Target-DB-Cluster",
                clusterSize = 3,
                provider = "GCP",
                clusterId = "target-123",
                primaryInstance = testPrimaryInstance,
                instances = testSecondaryInstances,
                clusterKeyFilePath = "/fake/path",
                instanceTypeId = "t2.micro",
            )

        state.savedClusters = listOf(targetCluster)
        assertNull(state.activeCluster)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SavedClustersScreen(state)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Target-DB-Cluster", substring = true, useUnmergedTree = true).performClick()

        assertNotNull(state.activeCluster)
        assertEquals(targetCluster, state.activeCluster)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Screen handles multiple providers and sizes correctly`() {
        val state = createTestState()
        state.savedClusters =
            listOf(
                LiveCluster(
                    clusterName = "GCP-Worker", clusterSize = 10, provider = "GCP",
                    clusterId = "id1", primaryInstance = testPrimaryInstance,
                    instances = testSecondaryInstances, clusterKeyFilePath = "",
                    instanceTypeId = "t2.micro",
                ),
                LiveCluster(
                    clusterName = "AWS-Micro", clusterSize = 1, provider = "AWS",
                    clusterId = "id2", primaryInstance = testPrimaryInstance,
                    instances = emptyList(), clusterKeyFilePath = "",
                    instanceTypeId = "t2.micro",
                ),
            )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides state.strings) {
                SavedClustersScreen(state)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("GCP-Worker", substring = true, useUnmergedTree = true).assertExists()
        val gcpTextFormat = state.strings.clusterNodeCount("GCP", 10)
        composeTestRule.onNodeWithText(gcpTextFormat, substring = true, useUnmergedTree = true).assertExists()

        composeTestRule.onNodeWithText("AWS-Micro", substring = true, useUnmergedTree = true).assertExists()
        val awsTextFormat = state.strings.clusterNodeCount("AWS", 1)
        composeTestRule.onNodeWithText(awsTextFormat, substring = true, useUnmergedTree = true).assertExists()
    }
}
