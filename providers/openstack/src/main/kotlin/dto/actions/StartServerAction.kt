package dto.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartServerAction(
    @SerialName("os-start")
    val start: String? = null,
)
