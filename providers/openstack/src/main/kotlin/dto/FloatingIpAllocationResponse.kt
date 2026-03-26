package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FloatingIpAllocationResponse(
    @SerialName("floating_ip")
    val floatingIp: AllocatedFloatingIp,
)
