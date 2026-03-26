package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FloatingIp(
    val address: String,
    @SerialName("fixed_address")
    val fixedAddress: String? = null,
)
