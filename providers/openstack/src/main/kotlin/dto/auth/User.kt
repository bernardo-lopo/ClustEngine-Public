package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val domain: Domain,
    val password: String,
)
