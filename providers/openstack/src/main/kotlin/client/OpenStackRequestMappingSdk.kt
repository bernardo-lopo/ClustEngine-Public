package client

import dto.actions.CreateInstanceRequest
import dto.auth.Auth
import dto.auth.AuthRequest
import dto.instance.Network
import dto.instance.SecurityGroup
import dto.instance.Server
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OpenStackRequestMappingSdk(val auth: Auth) {
    fun createCluster(
        clusterName: String,
        numberOfIntances: Int,
    ): String {
        val request =
            CreateInstanceRequest(
                server =
                    Server(
                        name = clusterName,
                        imageRef = "597fbc92-c33c-49f2-9793-d287b12690d9",
                        flavorRef = "20a6fa2c-6e11-4293-b3e9-4cae46b69a64",
                        availabilityZone = "nova",
                        diskConfig = "AUTO",
                        securityGroups =
                            listOf(
                                SecurityGroup(
                                    "default",
                                ),
                            ),
                        maxCount = numberOfIntances,
                        minCount = numberOfIntances,
                        networks =
                            listOf(
                                Network(
                                    "4dfa5963-ece6-473f-be43-c0ab48dfc4af",
                                ),
                            ),
                        keyName = "servers-fct",
                    ),
            )
        return Json.encodeToString(request)
    }

    fun getAuth(): String {
        val request = AuthRequest(auth)
        return Json.encodeToString(request)
    }
}
