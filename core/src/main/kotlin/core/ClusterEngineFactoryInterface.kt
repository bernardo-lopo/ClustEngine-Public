package core

import core.domain.IpRoutingMode
import core.domain.LiveCluster

interface ClusterEngineFactoryInterface {
    fun createFromSavedCluster(cluster: LiveCluster): ClusterServiceProviderInterface

    fun createAWSService(
        clusterName: String,
        clusterSize: Int,
        instanceTypeId: String,
        ipRoutingMode: IpRoutingMode,
    ): ClusterServiceProviderInterface

    fun createOpenStackService(
        clusterName: String,
        clusterSize: Int,
        instanceTypeId: String,
        ipRoutingMode: IpRoutingMode,
    ): ClusterServiceProviderInterface
}
