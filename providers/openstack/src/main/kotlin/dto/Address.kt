package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val addr: String,
    val version: Int,
    @SerialName("OS-EXT-IPS:type")
    val type: String? = null,
    @SerialName("OS-EXT-IPS-MAC:mac_addr")
    val macAddr: String? = null,
)
