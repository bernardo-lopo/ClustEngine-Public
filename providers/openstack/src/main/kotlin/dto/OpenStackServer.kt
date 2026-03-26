package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenStackServer(
    val id: String,
    val name: String,
    val status: String,
    @SerialName("OS-EXT-STS:task_state")
    val taskState: String? = null,
    @SerialName("OS-EXT-STS:vm_state")
    val vmState: String? = null,
    @SerialName("OS-EXT-STS:power_state")
    val powerState: Int? = null,
    val progress: Int? = null,
    val addresses: Map<String, List<Address>> = emptyMap(),
    val flavor: Flavor? = null,
    val image: Image? = null,
)
