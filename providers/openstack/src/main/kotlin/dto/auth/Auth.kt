package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val identity: Identity,
    val scope: Scope,
)
