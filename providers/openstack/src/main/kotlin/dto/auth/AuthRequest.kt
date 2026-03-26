package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val auth: Auth,
)
