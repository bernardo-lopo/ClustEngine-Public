import client.OpenStackExecuteRequest
import core.ClusterEngine
import core.ClusterEngineIO
import core.domain.ClustEngineInstance
import core.domain.IpRoutingMode
import core.domain.KeyAuth
import core.domain.MultiIpRouting
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.domain.SingleIpRouting
import core.util.ScriptConfigLoader
import dto.OpenStackServer
import dto.auth.Auth

class OpenStackClusterEngine(
    val fetchExecutionTimes: Boolean,
    val openStackExecuteRequest: OpenStackExecuteRequest,
    val clusterSize: Int,
    val clusterKey: KeyAuth,
    val clusterName: String,
    ipRoutingMode: IpRoutingMode,
    val auth: Auth,
    io: ClusterEngineIO,
) : ClusterEngine<OpenStackServer>(ipRoutingMode, io) {
    private suspend fun getToken() =
        openStackExecuteRequest.getAuth(
            auth,
        )

    override suspend fun isClusterInited(io: ClusterEngineIO): Boolean {
        val token = getToken()
        requireNotNull(token) {
            "Something went wrong, the token is null"
        }

        val allServers = openStackExecuteRequest.getAllInstances(token).servers

        return allServers.any { it.name.startsWith("$clusterName-") }
    }

    override suspend fun getClusterInstances(io: ClusterEngineIO): List<ClustEngineInstance> {
        val token = getToken()

        requireNotNull(token) {
            "Something went wrong, the token is null"
        }

        return openStackExecuteRequest.getAllInstances(token).servers.filter { it.name.startsWith("$clusterName-") }
            .map { it.mapInstanceToClustEngine() }
    }

    override suspend fun createCluster(
        io: ClusterEngineIO,
        ipRoutingMode: IpRoutingMode,
    ): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>> {
        // TODO("Cache the token to later use ?")
        val token = getToken()
        requireNotNull(token) {
            "Something went wrong, the token is null"
        }

        openStackExecuteRequest.createCluster(token, clusterName, clusterSize)

        /*
            To ensure that all the instances required where created it is needed to request to the fct cloud,
            all the instances in the cluster.
         */
        val currentInstances = openStackExecuteRequest.waitForInstanceState(token)
        requireNotNull(currentInstances) {
            "Timeout waiting for all servers to have IPs"
        }

        val clusterNodes = currentInstances.servers.filter { it.name.startsWith("$clusterName-") }
        require(clusterNodes.size == clusterSize) {
            "Expected $clusterSize instances for $clusterName, but found ${clusterNodes.size}."
        }

        val primaryInstance = currentInstances.servers.find { it.name == "$clusterName-1" }
        requireNotNull(primaryInstance) { "Could not find primary instance '$clusterName-1'" }

        val instancesRequiringPublicIps = mutableListOf<String>()

        when (ipRoutingMode) {
            is SingleIpRouting -> {
                val availablePublicIp = openStackExecuteRequest.getAvailablePublicIP(token)

                requireNotNull(availablePublicIp) {
                    "No public floating IPs available in the project. Cannot expose the primary node."
                }
                openStackExecuteRequest.addFloatingIPToInstance(token, primaryInstance.id, availablePublicIp)

                instancesRequiringPublicIps.add(primaryInstance.id)
            }

            is MultiIpRouting ->
                clusterNodes.forEach { node ->
                    val ip = openStackExecuteRequest.getAvailablePublicIP(token)
                    requireNotNull(ip) { "Not enough public floating IPs available for MULTI_IP mode. Please allocate more in OpenStack." }
                    openStackExecuteRequest.addFloatingIPToInstance(token, node.id, ip)

                    instancesRequiringPublicIps.add(node.id)
                }
        }

        val finalInstances = openStackExecuteRequest.waitForFloatingIps(token, instancesRequiringPublicIps)
        requireNotNull(finalInstances) {
            "Failed to verify Floating IP attachment within the timeout period."
        }

        val finalPrimaryInstance = finalInstances.servers.find { it.id == primaryInstance.id } ?: primaryInstance

        val currentClusterServers = finalInstances.servers.filter { it.name.startsWith("$clusterName-") }

        val clusterEngineInstances = mapInstancesToClustEngine(currentClusterServers)

        val primaryInstanceToDomain = finalPrimaryInstance.mapInstanceToClustEngine()

        return buildCluster(
            primaryInstance = primaryInstanceToDomain,
            instances = clusterEngineInstances,
            clusterKeyFilePath = clusterKey.keyFilePath,
            clusterSize = clusterSize,
            clusterName = clusterName,
            scriptPath = ScriptConfigLoader.SCRIPT_PATH,
            rebootInstance = { _ ->
                // The reboot is set to soft, explain in the documentation
                // openStackExecuteRequest.rebootInstance(token, instanceId)
            },
            waitUntilInstanceRunning = { _ ->
                openStackExecuteRequest.waitForInstanceState(token)
            },
            instanceTypeId = openStackExecuteRequest.openStackCloudRequests.flavorRefId,
            fetchExecutionTimes = fetchExecutionTimes,
        )
    }

    override suspend fun startInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val token = getToken()
        requireNotNull(token)

        val clusterInstances = getClusterInstances(io)

        val instance = clusterInstances.find { it.id == id }

        requireNotNull(instance)
        openStackExecuteRequest.startInstance(token, instance.id)
    }

    override suspend fun stopInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val token = getToken()

        requireNotNull(token)

        val clusterInstances = getClusterInstances(io)

        val instance = clusterInstances.find { it.id == id }
        requireNotNull(instance)
        openStackExecuteRequest.stopInstance(token, instance.id)
    }

    override suspend fun terminateClusterOrInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val token = getToken()

        requireNotNull(token) {
            "Token must not be null"
        }

        val clusterInstances = getClusterInstances(io)

        val instancesToTerminate: List<String>? =
            if (id != null) {
                listOf(id)
            } else {
                clusterInstances.map { it.id }
            }

        openStackExecuteRequest.terminateClusterOrInstance(token, instancesToTerminate)
    }

    override fun mapInstancesToClustEngine(instancesToMap: List<OpenStackServer>): List<ClustEngineInstance> {
        val mappedInstances = mutableListOf<ClustEngineInstance>()

        for (it in instancesToMap) {
            val clustEngineInstance = it.mapInstanceToClustEngine()

            mappedInstances.add(clustEngineInstance)
        }

        return mappedInstances
    }

    override fun OpenStackServer.mapInstanceToClustEngine(): ClustEngineInstance {
        val instanceId = this.id

        val allAddresses = this.addresses["thermions_net"] ?: emptyList()

        val privateIp =
            allAddresses.firstOrNull { it.type == "fixed" }?.addr ?: allAddresses.firstOrNull()?.addr ?: "N/A"

        val publicIp = allAddresses.firstOrNull { it.type == "floating" }?.addr ?: "localhost"

        return ClustEngineInstance(
            id = instanceId,
            privateIpAddress = privateIp,
            publicDns = publicIp,
            publicIp = publicIp,
        )
    }
}
