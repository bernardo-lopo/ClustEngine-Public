package core.domain

import kotlinx.serialization.Serializable

@Serializable
data class ClustEngineInstance(
    val id: String,
    val privateIpAddress: String,
    val publicDns: String,
    val publicIp: String,
)
