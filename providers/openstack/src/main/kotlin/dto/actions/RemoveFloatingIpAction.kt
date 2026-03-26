package dto.actions

import dto.FloatingIp
import kotlinx.serialization.Serializable

@Serializable
data class RemoveFloatingIpAction(
    val removeFloatingIp: FloatingIp,
)
