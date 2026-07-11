package core

import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ClusterManager")

open class ClusterManager(private val clusterService: ClusterServiceProviderInterface?) {
    fun isInited() = clusterService?.isClusterInited()

    fun initCluster() = init()

    fun start() {
        if (hasInitiated()) {
            logger.info("started all machines in the cluster")
            clusterService?.startAll()
        }
    }

    fun stop() {
        if (hasInitiated()) {
            logger.info("stopped all machines in the cluster")
            clusterService?.stopAll()
        }
    }

    fun listClusterInstances(): String? {
        if (!hasInitiated()) return null

        logger.info("Listing all instances of the cluster")
        val instances = clusterService?.listClusterInstances()

        if (instances!!.isEmpty()) return "There is no cluster instances available."

        return buildString {
            val header =
                "ID".padEnd(25) +
                    "PRIVATE IP".padEnd(18) +
                    "PUBLIC IP".padEnd(18) +
                    "PUBLIC DNS"

            appendLine(header)
            appendLine("-".repeat(header.length + 10))

            instances.forEach { instance ->
                val row =
                    instance.id.padEnd(25) +
                        instance.privateIpAddress.padEnd(18) +
                        instance.publicIp.padEnd(18) +
                        instance.publicDns

                appendLine(row)
            }
        }
    }

    fun deleteCluster() = clusterService?.deleteCluster()

    fun startById(id: String) = clusterService?.startById(id)

    fun stopById(id: String) = clusterService?.stopById(id)

    fun deleteInstance(id: String) = clusterService?.deleteInstance(id)

    private fun init(): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>>? {
        isInited()!!.let {
            if (!it) {
                return clusterService?.init()
            } else {
                logger.info("Cluster has already been initiated, use start/stop")
                return null
            }
        }
    }

    private fun hasInitiated(): Boolean {
        if (isInited() == true) {
            return true
        } else {
            logger.info("The cluster has not been initiated")
            return false
        }
    }
}
