package core

import core.domain.ClustEngineInstance
import core.domain.IpRoutingMode
import core.domain.MultiIpRouting
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.domain.SingleIpRouting
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
import java.util.concurrent.atomic.AtomicInteger

private const val PUBLIC_IP = "194.210.100.4"

private val logger: Logger = LoggerFactory.getLogger("ClusterEngine")

const val START_INSTANCE_IDX = 2222

abstract class ClusterEngine<S>(
    val ipRoutingMode: IpRoutingMode,
    val io: ClusterEngineIO,
) {
    abstract suspend fun isClusterInited(io: ClusterEngineIO): Boolean

    abstract suspend fun getClusterInstances(io: ClusterEngineIO): List<ClustEngineInstance>

    abstract suspend fun createCluster(io: ClusterEngineIO): Pair<PartialTimesPrimary, List<PartialTimesNonPrimary>>

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
                primaryInstancePublicIp = PUBLIC_IP,
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
        logger.info("!!!!!!!PIPELINE OPEN!!!!!!")
    }

    suspend fun buildCluster(
        primaryInstance: ClustEngineInstance,
        instances: List<ClustEngineInstance>,
        clusterKeyFilePath: String,
        clusterSize: Int,
        scriptPath: String,
        rebootInstance: suspend (instanceId: String) -> Unit,
        waitUntilInstanceRunning: suspend (isPrimary: Boolean) -> Unit,
    ): Pair<PartialTimesPrimary, List<PartialTimesNonPrimary>> =
        // The coroutineScope only terminates when all his children -> every launch, do so
        coroutineScope {
            val instanceIdxObj = AtomicInteger(START_INSTANCE_IDX)

            val primaryInstance = instances.find { it.id == primaryInstance.id }!!

            val partialTimesSecondary = mutableListOf<PartialTimesNonPrimary>()

            val usingPipeline = ipRoutingMode is SingleIpRouting

            try {
                logger.info("Running the script on the primary instance")
                val partialTimePrimary =
                    when (ipRoutingMode) {
                        is SingleIpRouting -> {
                            startSshPipeline(scriptPath, clusterKeyFilePath, primaryInstance, instances, instanceIdxObj)
                            io.runScriptOnInstanceUsingOnePublicIp(
                                primaryInstancePrivateIP = primaryInstance.privateIpAddress,
                                isPrimary = true,
                                clusterKeyFilePath = clusterKeyFilePath,
                                allInstances = instances,
                                instanceIdx = instanceIdxObj.get(),
                            )

                            io.getTimesFromMachineUsingOnePublicIp(
                                isPrimary = true,
                                clusterKeyFilePath = clusterKeyFilePath,
                                instanceIdxObj = instanceIdxObj,
                            ) as PartialTimesPrimary
                        }

                        is MultiIpRouting -> {
                            io.runScriptOnInstance(
                                primaryInstanceIP = primaryInstance.privateIpAddress,
                                publicIp = primaryInstance.publicDns,
                                isPrimary = true,
                                userOnInstance = "ubuntu",
                                scriptPath = scriptPath,
                                clusterKeyFilePath = clusterKeyFilePath,
                                allInstances = instances,
                            )

                            io.getTimesFromMachine(
                                isPrimary = true,
                                clusterKeyFilePath = clusterKeyFilePath,
                                publicIp = primaryInstance.publicIp,
                                instanceIdxObj = instanceIdxObj,
                            ) as PartialTimesPrimary
                        }
                    }

                logger.info("Rebooting the primary instance (public ip: ${primaryInstance.publicIp})...")

                if (usingPipeline) {
                    logger.info("Closing SSH tunnel before reboot...")
                    io.closeSshPipeline()
                }

                rebootInstance(primaryInstance.id)

                logger.info("Waiting until primary instance is running...")
                waitUntilInstanceRunning(true)

                // Re-open the tunnel after reboot
                if (usingPipeline) {
                    logger.info("Waiting 10s for SSHD to start after reboot...")
                    delay(10000)

                    logger.info("Re-establishing SSH Tunnel...")
                    startSshPipeline(
                        scriptPath,
                        clusterKeyFilePath,
                        primaryInstance,
                        instances,
                        instanceIdxObj,
                    )
                }

                logger.info("Successfully finished primary instance")

                val partialTimesSecondary = mutableListOf<PartialTimesNonPrimary>()
                val safeGuard = Mutex()

                val jobs =
                    instances
                        .filter { it.id != primaryInstance.id }
                        .mapIndexed { index, instance ->
                            launch(Dispatchers.IO) {
                                logger.info("Running the script on instance ${index + 2}")
                                val idx = instanceIdxObj.incrementAndGet()
                                try {
                                    val partialTimeSecondary =
                                        when (ipRoutingMode) {
                                            is SingleIpRouting -> {
                                                io.runScriptOnInstanceUsingOnePublicIp(
                                                    primaryInstancePrivateIP = primaryInstance.privateIpAddress,
                                                    isPrimary = false,
                                                    clusterKeyFilePath = clusterKeyFilePath,
                                                    allInstances = instances,
                                                    instanceIdx = idx,
                                                )

                                                io.getTimesFromMachineUsingOnePublicIp(
                                                    isPrimary = false,
                                                    clusterKeyFilePath = clusterKeyFilePath,
                                                    instanceIdxObj = instanceIdxObj,
                                                ) as PartialTimesNonPrimary
                                            }

                                            is MultiIpRouting -> {
                                                io.runScriptOnInstance(
                                                    primaryInstanceIP = primaryInstance.privateIpAddress,
                                                    publicIp = instance.publicDns,
                                                    isPrimary = false,
                                                    userOnInstance = "ubuntu",
                                                    scriptPath = scriptPath,
                                                    clusterKeyFilePath = clusterKeyFilePath,
                                                    allInstances = instances,
                                                )

                                                io.getTimesFromMachine(
                                                    isPrimary = false,
                                                    clusterKeyFilePath = clusterKeyFilePath,
                                                    publicIp = instance.publicIp,
                                                    instanceIdxObj = instanceIdxObj,
                                                ) as PartialTimesNonPrimary
                                            }
                                        }
                                    safeGuard.lock()
                                    partialTimesSecondary.add(partialTimeSecondary)
                                    safeGuard.unlock()
                                } catch (e: Exception) {
                                    logger.error("Error while processing instance ${instance.publicDns}: ${e.message}")
                                }
                            }
                        }

                logger.info("Successfully finished instance pool")
                jobs.joinAll()
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

    private fun cleanUpTemporaryFiles() {
        val tmpDir = File("tmp")
        if (tmpDir.exists()) {
            val deleted = tmpDir.deleteRecursively()
            if (deleted) {
                logger.info("Successfully deleted 'tmp' directory and all its contents.")
            } else {
                logger.warn("Failed to delete 'tmp' directory.")
            }
        }
    }
}
