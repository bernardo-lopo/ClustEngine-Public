package dto.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopServerAction(
    @SerialName("os-stop")
    val stop: String? = null,
)
