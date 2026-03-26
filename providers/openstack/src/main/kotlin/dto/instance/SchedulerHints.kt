package dto.instance

import kotlinx.serialization.Serializable

@Serializable
data class SchedulerHints(
    val sameHost: String,
)
