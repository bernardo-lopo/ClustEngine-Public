package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FloatingIpListResponse(
    @SerialName("floating_ips")
    val floatingIps: List<FloatingIpResponse>,
)
