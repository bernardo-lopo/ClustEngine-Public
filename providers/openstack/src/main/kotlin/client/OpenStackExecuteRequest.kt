package client

import core.data.FetchData
import dto.FloatingIp
import dto.FloatingIpListResponse
import dto.FloatingIpResponse
import dto.OpenStackServersResponse
import dto.RebootType
import dto.actions.AddFloatingIpAction
import dto.actions.RebootServerAction
import dto.actions.StartServerAction
import dto.actions.StopServerAction
import dto.auth.Auth
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.collections.isNotEmpty
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class OpenStackExecuteRequest(
    val openStack: OpenStackSdk,
    /*
          In openStackCloudRequests contains the reference to the object that contains all the request methods.
          These functions, if needed, return an object that latter will be converted to a json body.
     */
    val openStackCloudRequests: OpenStackRequestMappingSdk,
) {
    private val logger: Logger = LoggerFactory.getLogger("OpenStackExecuteRequest")

    // It is sent a post request, to create all the instances in the FCTCloud
    suspend fun createCluster(
        token: String,
        clusterName: String,
        clusterSize: Int,
    ) {
        // It is sent a post request, to create all the instances in the FCTCloud
        FetchData.post(
            body = openStackCloudRequests.createCluster(clusterName, clusterSize),
            url = openStack.createClusterUrl(),
            token = token,
        )
    }

    suspend fun getAuth(auth: Auth): String? {
        val requests =
            OpenStackRequestMappingSdk(
                auth,
                openStackCloudRequests.flavorRefId,
                openStackCloudRequests.imageRefId,
                openStackCloudRequests.availabilityZone,
                openStackCloudRequests.securityGroup,
                openStackCloudRequests.networkId,
                openStackCloudRequests.privateKeyName,
            )
        val body = requests.getAuth()
        val request =
            FetchData.post(
                body = body,
                url = openStack.getAuthToken(),
            )
        val token = request.header.entries.find { it.key == "x-subject-token" }?.value?.first()
        return token
    }

    suspend fun getAllInstances(token: String): OpenStackServersResponse {
        val request =
            FetchData.get(
                url = openStack.getAllIntanceDetailsUrl(),
                token = token,
            )

        println(request.body)

        val instances = JsonConfig.json.decodeFromString<OpenStackServersResponse>(request.body)

        return instances
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun rebootInstance(
        token: String,
        instanceId: String,
        type: String = "SOFT",
    ) {
        val json =
            Json {
                explicitNulls = true
                encodeDefaults = true
            }

        val action = RebootServerAction(RebootType(type))

        val body = json.encodeToString(action)

        val request =
            FetchData.post(
                url = openStack.performeActionOnIntance(instanceId),
                body = body,
                token = token,
            )

        if (request.status !in 200..299) {
            logger.error(
                "Failed to reboot instance $instanceId. " + "Status: ${request.status}, Body: ${request.body}",
            )
        }
    }

    private suspend fun getClusterState(token: String): OpenStackServersResponse? {
        val instances = getAllInstances(token)

        val allReady =
            instances.servers.all { server ->
                server.status == "ACTIVE" && server.taskState == null && server.addresses.values.all { it.isNotEmpty() }
            }
        return if (allReady && instances.servers.isNotEmpty()) instances else null
    }

    suspend fun waitForInstanceState(token: String): OpenStackServersResponse? {
        // In this function it is made pooling to get all the information needed from all instances.
        return try {
            withTimeout(5.minutes) {
                while (true) {
                    val clusterState = getClusterState(token)

                    if (clusterState != null) return@withTimeout clusterState

                    delay(5000.milliseconds)
                }
            }
        } catch (_: TimeoutCancellationException) {
            // Timeout reached
            null
        } as OpenStackServersResponse?
    }

    suspend fun waitForFloatingIps(
        token: String,
        instanceIds: List<String>,
    ): OpenStackServersResponse? {
        // In this function it is made pooling to wait for Floating IPs to be attached.
        return try {
            withTimeout(2.minutes) {
                while (true) {
                    val instances = getAllInstances(token)

                    val targetServers = instances.servers.filter { it.id in instanceIds }

                    val allReady =
                        targetServers.isNotEmpty() &&
                            targetServers.all { server ->
                                // Check all network interfaces on the server for a floating IP
                                server.addresses.values.any { addressList ->
                                    addressList.any { it.type == "floating" }
                                }
                            }

                    if (allReady) return@withTimeout instances

                    logger.info("Waiting for Floating IPs to attach to instances... ")
                    delay(2000.milliseconds)
                }
            }
        } catch (_: TimeoutCancellationException) {
            logger.warn("Timeout waiting for Floating IPs to attach to instances: $instanceIds")
            null
        } as OpenStackServersResponse?
    }

    suspend fun terminateClusterOrInstance(
        token: String,
        instancesToDelete: List<String>?,
    ): Boolean {
        requireNotNull(instancesToDelete)
        instancesToDelete.forEach { instanceId ->
            val request =
                FetchData.delete(
                    url = openStack.deleteIntanceUrl(instanceId),
                    token = token,
                )
            if (request.status !in 200..299) {
                logger.error("An error has occur during the delete process of instance $instanceId")
                return false
            }
        }
        return true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun stopInstance(
        token: String,
        instanceId: String,
    ) {
        val json =
            Json {
                explicitNulls = true
                encodeDefaults = true
            }

        val body = json.encodeToString(StopServerAction())

        FetchData.post(
            url = openStack.stopIntanceUrl(instanceId),
            body = body,
            token = token,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun startInstance(
        token: String,
        instanceId: String,
    ) {
        val json =
            Json {
                explicitNulls = true
                encodeDefaults = true
            }

        val body = json.encodeToString(StartServerAction())

        FetchData.post(
            url = openStack.startIntanceUrl(instanceId),
            body = body,
            token = token,
        )
    }

    suspend fun getAvailablePublicIP(token: String): FloatingIpResponse? {
        val response =
            FetchData.get(
                url = openStack.getAvailablePublicIP(),
                token = token,
            )

        println("FLOATING IPS JSON: ${response.body}")

        val ipListResponse = Json.decodeFromString<FloatingIpListResponse>(response.body)
        val availablePublicIP = ipListResponse.floatingIps.firstOrNull { it.instanceId == null }

        return availablePublicIP
    }

    suspend fun addFloatingIPToInstance(
        token: String,
        instanceId: String,
        availablePublicIP: FloatingIpResponse,
    ) {
        val body =
            Json.encodeToString(
                AddFloatingIpAction(
                    FloatingIp(address = availablePublicIP.ip),
                ),
            )

        FetchData.post(
            url = openStack.performeActionOnIntance(instanceId),
            body = body,
            token = token,
        )
    }
}
