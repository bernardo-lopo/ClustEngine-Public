package dto

import kotlinx.serialization.Serializable

@Serializable
/**
 It can be HARD or SOFT, by default is set to SOFT.
 The differences:
 SOFT -> The reboot will attempt a graceful shutdown and restart of the server
 HARD -> The reboot will do a forced shutdown and restart of the server.
 The HARD reboot corresponds to the power cycles of the server.
 */
data class RebootType(val type: String = "SOFT")
