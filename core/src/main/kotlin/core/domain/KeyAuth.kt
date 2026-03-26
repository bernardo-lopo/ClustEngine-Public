package core.domain

import kotlinx.serialization.Serializable

/**
 * Represents the authentication key file details required for accessing a resource.
 *
 * @param fileName The name of the authentication key file.
 * @param keyName The name of the authentication key.
 * @param keyFilePath The file system path to the authentication key file.
 */
@Serializable
data class KeyAuth(
    val fileName: String,
    // val keyName: String,
    val keyFilePath: String,
)
