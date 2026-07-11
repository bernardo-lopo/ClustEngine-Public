package core.domain

import kotlinx.serialization.Serializable

@Serializable
data class LiveCluster(
    val clusterId: String,
    val clusterName: String,
    val provider: String,
    val instanceTypeId: String?,
    val primaryInstance: ClustEngineInstance,
    val instances: List<ClustEngineInstance>,
    val clusterKeyFilePath: String,
    val clusterSize: Int,
)
