package dto.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerMetadata(
    @SerialName("My Server Name")
    val serverName: String,
)
