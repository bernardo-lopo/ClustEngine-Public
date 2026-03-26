package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class Password(
    val user: User,
)
