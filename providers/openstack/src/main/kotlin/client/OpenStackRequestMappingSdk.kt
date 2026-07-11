package client

import dto.actions.CreateInstanceRequest
import dto.auth.Auth
import dto.auth.AuthRequest
import dto.instance.Network
import dto.instance.SecurityGroup
import dto.instance.Server
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OpenStackRequestMappingSdk(
    val auth: Auth,
    val flavorRefId: String,
    val imageRefId: String,
    val availabilityZone: String,
    val securityGroup: String,
    val networkId: String,
    val privateKeyName: String,
) {
    fun createCluster(
        clusterName: String,
        numberOfIntances: Int,
    ): String {
        val request =
            CreateInstanceRequest(
                server =
                    Server(
                        name = clusterName,
                        imageRef = imageRefId,
                        flavorRef = flavorRefId,
                        availabilityZone = availabilityZone,
                        diskConfig = "AUTO",
                        securityGroups =
                            listOf(
                                SecurityGroup(
                                    securityGroup,
                                ),
                            ),
                        maxCount = numberOfIntances,
                        minCount = numberOfIntances,
                        networks =
                            listOf(
                                Network(
                                    networkId,
                                ),
                            ),
                        keyName = privateKeyName,
                    ),
            )
        return Json.encodeToString(request)
    }

    fun getAuth(): String {
        val request = AuthRequest(auth)
        return Json.encodeToString(request)
    }
}
