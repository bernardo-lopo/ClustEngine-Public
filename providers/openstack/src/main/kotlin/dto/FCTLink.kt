package dto

import kotlinx.serialization.Serializable

@Serializable
data class FCTLink(
    val rel: String,
    val href: String,
)
