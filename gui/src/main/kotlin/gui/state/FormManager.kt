package gui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.domain.ClustEngineInstanceType
import gui.state.enums.CloudProvider
import gui.state.enums.UIRoutingMode

class FormManager {
    var clusterName by mutableStateOf("")
    var clusterSize by mutableStateOf("")
    var selectedProvider by mutableStateOf<CloudProvider?>(null)
    var availableFlavors by mutableStateOf<List<ClustEngineInstanceType>>(emptyList())
    var selectedFlavor by mutableStateOf<ClustEngineInstanceType?>(null)
    var selectedRoutingMode by mutableStateOf<UIRoutingMode?>(null)

    fun clear() {
        clusterName = ""
        clusterSize = ""
        selectedProvider = null
        availableFlavors = emptyList()
        selectedFlavor = null
        selectedRoutingMode = null
    }
}
