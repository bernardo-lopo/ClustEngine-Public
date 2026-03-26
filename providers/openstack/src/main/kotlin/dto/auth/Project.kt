package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val name: String,
    val domain: Domain,
)
