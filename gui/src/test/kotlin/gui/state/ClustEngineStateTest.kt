package gui.state

import core.ClusterEngineFactoryInterface
import core.domain.ClustEngineInstance
import core.domain.LiveCluster
import gui.state.enums.AppScreen
import gui.state.enums.CloudProvider
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClustEngineStateTest {
    private val testPrimaryInstance =
        ClustEngineInstance(
            id = "1",
            privateIpAddress = "10.0.0.1",
            publicDns = "primary.dns",
            publicIp = "123.123.123.1",
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createFake(): ClustEngineState {
        val mockFactory = mockk<ClusterEngineFactoryInterface>(relaxed = true)
        return ClustEngineState(
            coroutineScope = TestScope(UnconfinedTestDispatcher()),
            clusterEngineFactory = mockFactory,
        )
    }

    @Test
    fun `Fake delegates form properties correctly`() {
        val state = createFake()

        state.clusterName = "Fake-Cluster"
        state.selectedProvider = CloudProvider.AWS

        assertEquals("Fake-Cluster", state.clusterName)
        assertEquals(CloudProvider.AWS, state.selectedProvider)
    }

    @Test
    fun `MapsToSetup clears form and console, then navigates`() {
        val state = createFake()

        state.clusterName = "Old-Name"
        state.navigateTo(AppScreen.Dashboard)

        state.navigateToSetup()

        assertEquals("", state.clusterName)
        assertEquals(AppScreen.Setup, state.currentScreen)
        assertTrue(state.consoleLogs.isEmpty())
    }

    @Test
    fun `selectAndNavigateToDashboard populates form and updates active cluster`() {
        val state = createFake()
        val targetCluster =
            LiveCluster(
                clusterName = "Production-DB",
                clusterSize = 5,
                provider = "OpenStack",
                clusterId = "id-123",
                primaryInstance = testPrimaryInstance,
                instances = emptyList(),
                clusterKeyFilePath = "/path",
                instanceTypeId = "m1.large",
            )

        state.selectAndNavigateToDashboard(targetCluster)

        assertEquals(targetCluster, state.activeCluster)

        assertEquals("Production-DB", state.clusterName)
        assertEquals("5", state.clusterSize)
        assertEquals(CloudProvider.OpenStack, state.selectedProvider)

        assertEquals(AppScreen.Dashboard, state.currentScreen)
    }

    @Test
    fun `cancelClusterCreation clears job and engine state`() {
        val state = createFake()

        state.cancelClusterCreation()

        assertNull(state.activeCluster)
        assertEquals(0L, state.creationTimeInSeconds)
    }
}
