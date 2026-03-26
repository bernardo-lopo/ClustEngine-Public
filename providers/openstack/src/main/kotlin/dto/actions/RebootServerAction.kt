package dto.actions

import dto.RebootType
import kotlinx.serialization.Serializable

@Serializable
data class RebootServerAction(val reboot: RebootType = RebootType())
