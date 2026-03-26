package providers.openstack

import client.OpenStackExecuteRequest
import core.ClusterEngine
import core.ClusterEngineIO
import core.SCRIPT_PATH
import core.domain.ClustEngineInstance
import core.domain.IpRoutingMode
import core.domain.KeyAuth
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import dto.OpenStackServer
import dto.auth.Auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("OpenStackClusterEngine")

const val PRIMARY_INSTANCE_PUBLIC_IP = "194.210.100.4"

class OpenStackClusterEngine(
    val openStackExecuteRequest: OpenStackExecuteRequest,
    val clusterSize: Int,
    val clusterKey: KeyAuth,
    val clusterName: String,
    ipRoutingMode: IpRoutingMode,
    val auth: Auth,
    io: ClusterEngineIO,
) : ClusterEngine<OpenStackServer>(ipRoutingMode, io) {
    override suspend fun isClusterInited(io: ClusterEngineIO): Boolean {
        val token = openStackExecuteRequest.getAuth(auth)

        requireNotNull(token) {
            "Something went wrong, the token is null"
        }

        return openStackExecuteRequest.isClusterInited(token)
    }

    override suspend fun getClusterInstances(io: ClusterEngineIO): List<ClustEngineInstance> {
        val token = openStackExecuteRequest.getAuth(auth)

        requireNotNull(token) {
            "Something went wrong, the token is null"
        }

        return openStackExecuteRequest.getAllInstances(token).servers.map { it.mapInstanceToClustEngine() }
    }

    override suspend fun createCluster(io: ClusterEngineIO): Pair<PartialTimesPrimary, List<PartialTimesNonPrimary>> {
        // TODO("Cache the token to later use ?")
        val token = openStackExecuteRequest.getAuth(auth)
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

        require(currentInstances.servers.size == clusterSize)

        val primaryInstance = currentInstances.servers.find { it.name == "$clusterName-1" }
        requireNotNull(primaryInstance) { "Could not find primary instance '$clusterName-1'" }

        openStackExecuteRequest.addFloatingIPToInstance(token, primaryInstance.id)

        val clusterEngineInstances = mapInstancesToClustEngine(currentInstances.servers)

        val primaryInstanceToDomain = primaryInstance.mapInstanceToClustEngine()

        return buildCluster(
            primaryInstance = primaryInstanceToDomain,
            instances = clusterEngineInstances,
            clusterKeyFilePath = clusterKey.keyFilePath,
            clusterSize = clusterSize,
            scriptPath = SCRIPT_PATH,
            rebootInstance = { _ ->
                // The reboot is set to soft, explain in the documentation
                // openStackExecuteRequest.rebootInstance(token, instanceId)
            },
            waitUntilInstanceRunning = { isPrimary ->
                openStackExecuteRequest.waitForInstanceState(token, isPrimary)
            },
        )
    }

    override suspend fun startInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val token = openStackExecuteRequest.getAuth(auth)
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
        val token = openStackExecuteRequest.getAuth(auth)

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
        val token =
            requireNotNull(openStackExecuteRequest.getAuth(auth)) {
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
        val id = this.id
        val privateIp = this.addresses["thermions_net"]?.get(0)?.addr.toString()
        val publicDns = ""
        // To be changed, the public ip should not be hardcoded
        val publicIp = if (this.name == "$clusterName-1") PRIMARY_INSTANCE_PUBLIC_IP else "localhost"

        return ClustEngineInstance(
            id = id,
            privateIpAddress = privateIp,
            publicDns = publicDns,
            publicIp = publicIp,
        )
    }
}
