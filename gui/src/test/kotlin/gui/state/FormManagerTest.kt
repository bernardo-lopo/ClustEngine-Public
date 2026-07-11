package gui.state

import core.domain.ClustEngineInstanceType
import gui.state.enums.CloudProvider
import gui.state.enums.UIRoutingMode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormManagerTest {
    @Test
    fun `FormManager stores and updates values correctly`() {
        val form = FormManager()

        form.clusterName = "Test-Cluster"
        form.clusterSize = "10"
        form.selectedProvider = CloudProvider.AWS
        form.selectedRoutingMode = UIRoutingMode.SINGLE_IP

        assertEquals("Test-Cluster", form.clusterName)
        assertEquals("10", form.clusterSize)
        assertEquals(CloudProvider.AWS, form.selectedProvider)
        assertEquals(UIRoutingMode.SINGLE_IP, form.selectedRoutingMode)
    }

    @Test
    fun `clear() resets all form fields to default state`() {
        val form = FormManager()

        // Populate
        form.clusterName = "Dirty-Form"
        form.clusterSize = "50"
        form.selectedProvider = CloudProvider.OpenStack
        form.availableFlavors = listOf(ClustEngineInstanceType("id", "name", "specs"))
        form.selectedFlavor = form.availableFlavors.first()
        form.selectedRoutingMode = UIRoutingMode.MULTI_IP

        // Action
        form.clear()

        // Assert
        assertEquals("", form.clusterName)
        assertEquals("", form.clusterSize)
        assertNull(form.selectedProvider)
        assertTrue(form.availableFlavors.isEmpty())
        assertNull(form.selectedFlavor)
        assertNull(form.selectedRoutingMode)
    }
}
