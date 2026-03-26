package dto.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Server(
    // val accessIPv4: String,
    // val accessIPv6: String,
    val name: String,
    val imageRef: String,
    val flavorRef: String,
    @SerialName("availability_zone")
    val availabilityZone: String,
    @SerialName("OS-DCF:diskConfig")
    val diskConfig: String,
    @SerialName("min_count")
    val minCount: Int,
    @SerialName("max_count")
    val maxCount: Int,
    @SerialName("security_groups")
    val securityGroups: List<SecurityGroup>,
    val networks: List<Network>,
    @SerialName("key_name")
    val keyName: String,
)
