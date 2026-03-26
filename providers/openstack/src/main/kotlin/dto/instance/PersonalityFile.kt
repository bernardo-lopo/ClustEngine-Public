package dto.instance

import kotlinx.serialization.Serializable

@Serializable
data class PersonalityFile(
    val path: String,
    val contents: String,
)
