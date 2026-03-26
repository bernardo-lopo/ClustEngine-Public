package dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class Identity(
    val methods: List<String>,
    val password: Password,
)
