package core

import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ClusterManager")

open class ClusterManager(private val clusterService: ClusterServiceProviderInterface) {
    val isInited
        get() = clusterService.isClusterInited()

    val initCluster
        get() = init()

    val start
        get() =
            run {
                if (hasInitiated()) {
                    logger.info("started all machines in the cluster")
                    clusterService.startAll()
                }
            }

    val stop
        get() =
            run {
                if (hasInitiated()) {
                    logger.info("stopped all machines in the cluster")
                    clusterService.stopAll()
                }
            }

    val deleteCluster
        get() = clusterService.deleteCluster()

    fun startById(id: String) = clusterService.startById(id)

    fun stopById(id: String) = clusterService.stopById(id)

    fun deleteInstance(id: String) = clusterService.deleteInstance(id)

    private fun init(): Pair<PartialTimesPrimary, List<PartialTimesNonPrimary>>? {
        if (!isInited) {
            return clusterService.init()
        } else {
            logger.info("Cluster has already been initiated, use start/stop")
            return null
        }
    }

    private fun hasInitiated(): Boolean {
        if (isInited) {
            return true
        } else {
            logger.info("The cluster has not been initiated")
            return false
        }
    }
}
