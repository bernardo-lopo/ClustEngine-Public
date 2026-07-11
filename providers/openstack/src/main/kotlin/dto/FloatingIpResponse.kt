package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FloatingIpResponse(
    val id: String,
    val ip: String,
    val pool: String? = null,
    @SerialName("fixed_ip")
    val fixedIp: String? = null,
    @SerialName("instance_id")
    val instanceId: String? = null,
)
