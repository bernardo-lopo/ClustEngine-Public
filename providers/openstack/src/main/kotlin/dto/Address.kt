package dto

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val addr: String,
    val version: Int,
)
