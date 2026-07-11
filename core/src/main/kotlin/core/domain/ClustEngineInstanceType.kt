package core.domain

import kotlinx.serialization.Serializable

@Serializable
data class ClustEngineInstanceType(
    val id: String,
    val displayName: String,
    val specs: String,
)
