package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllocatedFloatingIp(
    val id: String,
    val pool: String,
    @SerialName("instance_id")
    val instanceId: String? = null,
    val ip: String,
    @SerialName("fixed_ip")
    val fixedIp: String? = null,
)
