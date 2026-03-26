package dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenStackServersResponse(
    val servers: List<OpenStackServer>,
)
