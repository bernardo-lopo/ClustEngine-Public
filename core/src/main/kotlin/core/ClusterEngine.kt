package core

import core.domain.ClustEngineInstance
import core.domain.IpRoutingMode
import core.domain.LiveCluster
import core.domain.MultiIpRouting
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.domain.SingleIpRouting
import core.persistance.ClusterPersistenceManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

private val logger: Logger = LoggerFactory.getLogger("ClusterEngine")

const val START_INSTANCE_IDX = 2222

abstract class ClusterEngine<S>(
    val ipRoutingMode: IpRoutingMode,
    val io: ClusterEngineIO,
) {
    abstract suspend fun isClusterInited(io: ClusterEngineIO): Boolean

    abstract suspend fun getClusterInstances(io: ClusterEngineIO): List<ClustEngineInstance>

    abstract suspend fun createCluster(
        io: ClusterEngineIO,
        ipRoutingMode: IpRoutingMode,
    ): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>>

    abstract suspend fun startInstance(
        io: ClusterEngineIO,
        id: String?,
    )

    abstract suspend fun stopInstance(
        io: ClusterEngineIO,
        id: String?,
    )

    abstract suspend fun terminateClusterOrInstance(
        io: ClusterEngineIO,
        id: String?,
    )

    abstract fun mapInstancesToClustEngine(instancesToMap: List<S>): List<ClustEngineInstance>

    abstract fun S.mapInstanceToClustEngine(): ClustEngineInstance

    private suspend fun CoroutineScope.startSshPipeline(
        scriptPath: String,
        clusterKeyFilePath: String,
        primaryInstance: ClustEngineInstance,
        instances: List<ClustEngineInstance>,
        instanceIdxObj: AtomicInteger,
    ) {
        logger.info("Starting ssh pipeline...")
        // Signal object to give the await a way to stop waiting.
        val pipelineReady = CompletableDeferred<Unit>()
        launch {
            io.prepareSshPipeline(
                primaryInstancePublicIp = primaryInstance.publicIp,
                scriptPath = scriptPath,
                clusterKeyFilePath = clusterKeyFilePath,
                allInstances = instances,
                onPipelineReady = { pipelineReady.complete(Unit) },
                instanceIdxObj = instanceIdxObj,
                primaryInstance = primaryInstance,
            )
        }
        logger.info("Waiting for SSH tunnel...")
        pipelineReady.await()
        logger.info("SSH Tunnel ready.")
    }

    suspend fun deleteCluster(
        io: ClusterEngineIO,
        clusterId: String,
    ) {
        logger.info("Initiating full cluster termination for ID: $clusterId")
        try {
            terminateClusterOrInstance(io, null)
            logger.info("Cloud infrastructure terminated. Removing local configuration...")
            ClusterPersistenceManager.deleteCluster(clusterId)
            logger.info("Cluster successfully destroyed and removed from UI.")
        } catch (e: Exception) {
            logger.error("Failed to complete cluster termination: ${e.message}")
            throw e
        }
    }

    suspend fun buildCluster(
        primaryInstance: ClustEngineInstance,
        instances: List<ClustEngineInstance>,
        clusterKeyFilePath: String,
        clusterName: String,
        clusterSize: Int,
        instanceTypeId: String,
        scriptPath: String,
        rebootInstance: suspend (instanceId: String) -> Unit,
        fetchExecutionTimes: Boolean,
        waitUntilInstanceRunning: suspend (isPrimary: Boolean) -> Unit,
    ): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>> =
        // The coroutineScope only terminates when all his children -> every launch, do so
        coroutineScope {
            require(!ClusterPersistenceManager.clusterExists(clusterName)) {
                "A cluster with the name '$clusterName' already exists locally. Aborting build."
            }

            val instanceIdxObj = AtomicInteger(START_INSTANCE_IDX)

            val primaryInstance = instances.find { it.id == primaryInstance.id }!!

            val usingPipeline = ipRoutingMode is SingleIpRouting

            val safeGuard = Mutex()

            try {
                logger.info("Running the script on the primary instance")
                logger.info("Waiting 15s to finish the setup on the instances...")
                delay(15000)

                if (usingPipeline) {
                    startSshPipeline(scriptPath, clusterKeyFilePath, primaryInstance, instances, instanceIdxObj)
                }

                val partialTimePrimary =
                    when (
                        val result =
                            executeScriptWithRouting(
                                io,
                                primaryInstance,
                                true,
                                primaryInstance,
                                instances,
                                clusterKeyFilePath,
                                instanceIdxObj.get(),
                                fetchExecutionTimes,
                            )
                    ) {
                        is PartialTimesPrimary -> result
                        else -> null
                    }

                logger.info("Rebooting the primary instance with public ip: ${primaryInstance.publicIp}...")

                if (usingPipeline) {
                    logger.info("Closing SSH tunnel before reboot...")
                    io.closeSshPipeline()
                }

                rebootInstance(primaryInstance.id)

                logger.info("Waiting until primary instance is running...")
                waitUntilInstanceRunning(true)

                if (usingPipeline) {
                    logger.info("Waiting 10s for SSHD to start after reboot...")
                    delay(10000)
                    logger.info("Re-establishing SSH Tunnel...")
                    startSshPipeline(scriptPath, clusterKeyFilePath, primaryInstance, instances, instanceIdxObj)
                }

                logger.info("Successfully finished primary instance")

                val partialTimesSecondary = mutableListOf<PartialTimesNonPrimary?>()

                val jobs =
                    instances.filter { it.id != primaryInstance.id }.mapIndexed { index, instance ->
                        launch(Dispatchers.IO) {
                            logger.info("Running the script on instance ${index + 2}")
                            val idx = instanceIdxObj.incrementAndGet()
                            try {
                                val partialTimeSecondary =
                                    when (
                                        val result =
                                            executeScriptWithRouting(
                                                io,
                                                instance,
                                                false,
                                                primaryInstance,
                                                instances,
                                                clusterKeyFilePath,
                                                idx,
                                                fetchExecutionTimes,
                                            )
                                    ) {
                                        is PartialTimesNonPrimary -> result
                                        else -> null
                                    }

                                if (partialTimeSecondary != null) {
                                    safeGuard.lock()
                                    partialTimesSecondary.add(partialTimeSecondary)
                                    safeGuard.unlock()
                                }
                            } catch (e: Exception) {
                                logger.error("Error while processing instance ${instance.publicDns}: ${e.message}")
                            }
                        }
                    }

                logger.info("Successfully finished instance pool")
                jobs.joinAll()

                val newLiveCluster =
                    LiveCluster(
                        clusterId = UUID.randomUUID().toString(),
                        clusterName = clusterName,
                        provider = this@ClusterEngine.javaClass.simpleName,
                        primaryInstance = primaryInstance,
                        instances = instances,
                        clusterKeyFilePath = clusterKeyFilePath,
                        clusterSize = clusterSize,
                        instanceTypeId = instanceTypeId,
                    )

                ClusterPersistenceManager.saveCluster(newLiveCluster)

                return@coroutineScope Pair(partialTimePrimary, partialTimesSecondary)
            } finally {
                if (usingPipeline) {
                    logger.info("Closing ssh pipeline...")
                    io.closeSshPipeline()
                }
                logger.info("Cleaning up temporary files...")
                cleanUpTemporaryFiles()
            }
        }

    private suspend fun executeScriptWithRouting(
        io: ClusterEngineIO,
        instance: ClustEngineInstance,
        isPrimary: Boolean,
        primaryInstance: ClustEngineInstance,
        instances: List<ClustEngineInstance>,
        clusterKeyFilePath: String,
        idx: Int,
        fetchTimes: Boolean,
    ): Any? {
        when (ipRoutingMode) {
            is SingleIpRouting ->
                io.runScriptOnInstanceUsingOnePublicIp(
                    primaryInstancePrivateIP = primaryInstance.privateIpAddress,
                    isPrimary = isPrimary,
                    clusterKeyFilePath = clusterKeyFilePath,
                    allInstances = instances,
                    instanceIdx = idx,
                )

            is MultiIpRouting ->
                io.runScriptOnInstance(
                    primaryInstanceIP = primaryInstance.privateIpAddress,
                    publicIp = if (isPrimary) primaryInstance.publicDns else instance.publicDns,
                    isPrimary = isPrimary,
                    clusterKeyFilePath = clusterKeyFilePath,
                    allInstances = instances,
                )
        }

        if (!fetchTimes) return null

        return when (ipRoutingMode) {
            is SingleIpRouting ->
                io.getTimesFromMachineUsingOnePublicIp(
                    isPrimary,
                    clusterKeyFilePath,
                    instanceIdxObj = AtomicInteger(idx),
                )

            is MultiIpRouting ->
                io.getTimesFromMachine(
                    isPrimary,
                    clusterKeyFilePath,
                    instance.publicIp,
                    instanceIdxObj = AtomicInteger(idx),
                    publicIp = instance.publicIp,
                )
        }
    }

    private fun getAppRootDir(): File {
        val currentDir = File(System.getProperty("user.dir")).absoluteFile
        val submodules = setOf("app", "core", "providers", "tui", "tester")

        return if (submodules.contains(currentDir.name)) {
            currentDir.parentFile
        } else {
            currentDir
        }
    }

    private fun cleanUpTemporaryFiles() {
        val rootDir = getAppRootDir()

        val foldersToDelete = listOf("tmp", "temp", "app/tmp")

        foldersToDelete.forEach { folderPath ->
            val targetFolder = File(rootDir, folderPath)
            if (targetFolder.exists()) {
                val deleted = targetFolder.deleteRecursively()
                if (deleted) {
                    logger.info("Successfully deleted: ${targetFolder.absolutePath}")
                } else {
                    logger.warn("Failed to delete: ${targetFolder.absolutePath}")
                }
            }
        }
    }
}
