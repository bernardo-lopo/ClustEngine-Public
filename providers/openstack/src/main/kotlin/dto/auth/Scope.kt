package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class Scope(
    val project: Project,
)
