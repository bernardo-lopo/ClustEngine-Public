package dto

import kotlinx.serialization.Serializable

/**
 * Represents an instance object with details about a computing resource.
 *
 * @property name The name of the instance.
 * @property id The unique identifier of the instance.
 * @property privateIp The private IP address of the instance.
 * @property publicIp The public IP address of the instance.
 * @property state The current state of the instance (e.g., running, stopped).
 */
@Serializable
data class AWSServer(
    val name: String,
    val id: String,
    val privateIp: String,
    val publicIp: String,
    val state: String,
)
