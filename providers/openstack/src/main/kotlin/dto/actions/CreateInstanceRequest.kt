package dto.actions

import dto.instance.Server
import kotlinx.serialization.Serializable

@Serializable
data class CreateInstanceRequest(
    val server: Server,
)
