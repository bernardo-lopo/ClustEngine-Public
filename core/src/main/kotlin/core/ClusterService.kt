package core

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import core.domain.ClustEngineInstance
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

class ClusterService<S>(
    val clusterEngine: ClusterEngine<S>,
    val clusterEngineIO: ClusterEngineIO,
) : ClusterServiceProviderInterface {
    val instances
        get() =
            runBlocking(Dispatchers.IO) {
                clusterEngine.getClusterInstances(clusterEngineIO)
            }

    private val mapper = jacksonObjectMapper()

    override fun isClusterInited(): Boolean =
        runBlocking(Dispatchers.IO) {
            clusterEngine.isClusterInited(clusterEngineIO)
        }

    private fun createCluster(): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>> =
        runBlocking(Dispatchers.IO) {
            return@runBlocking clusterEngine.createCluster(clusterEngineIO, clusterEngine.ipRoutingMode)
        }

    override fun init(): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>> {
        val data: Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>>
        runBlocking(Dispatchers.IO) {
            data = createCluster()
            writeActiveInstances()
        }
        return data
    }

    fun writeActiveInstances() {
        val instances =
            runBlocking(Dispatchers.IO) {
                clusterEngine.getClusterInstances(clusterEngineIO)
            }

        val data = mapOf("instanceList" to instances)

        val instancesInJson =
            mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data)

        File("active_instances.json").writeText(instancesInJson)
    }

    override fun startAll() {
        runBlocking(Dispatchers.IO) {
            for (instance in instances) {
                clusterEngine.startInstance(clusterEngineIO, instance.id)
            }
        }
    }

    override fun listClusterInstances(): List<ClustEngineInstance> =
        runBlocking(Dispatchers.IO) {
            return@runBlocking clusterEngine.getClusterInstances(clusterEngineIO)
        }

    override fun stopAll() {
        runBlocking(Dispatchers.IO) {
            for (instance in instances) {
                clusterEngine.stopInstance(clusterEngineIO, instance.id)
            }
        }
    }

    override fun deleteCluster() {
        runBlocking(Dispatchers.IO) {
            clusterEngine.terminateClusterOrInstance(clusterEngineIO, null)
        }
    }

    override fun startById(id: String) {
        runBlocking(Dispatchers.IO) {
            clusterEngine.startInstance(clusterEngineIO, id)
        }
    }

    override fun stopById(id: String) {
        runBlocking(Dispatchers.IO) {
            clusterEngine.stopInstance(clusterEngineIO, id)
        }
    }

    override fun deleteInstance(id: String) {
        runBlocking(Dispatchers.IO) {
            clusterEngine.terminateClusterOrInstance(clusterEngineIO, id)
        }
    }
}
