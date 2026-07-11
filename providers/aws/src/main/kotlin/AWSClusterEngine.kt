import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.describeInstances
import aws.sdk.kotlin.services.ec2.model.CreateTagsRequest
import aws.sdk.kotlin.services.ec2.model.Instance
import aws.sdk.kotlin.services.ec2.model.InstanceType
import aws.sdk.kotlin.services.ec2.model.RunInstancesRequest
import aws.sdk.kotlin.services.ec2.model.StartInstancesRequest
import aws.sdk.kotlin.services.ec2.model.StopInstancesRequest
import aws.sdk.kotlin.services.ec2.model.Tag
import aws.sdk.kotlin.services.ec2.rebootInstances
import aws.sdk.kotlin.services.ec2.terminateInstances
import aws.sdk.kotlin.services.ec2.waiters.waitUntilInstanceStatusOk
import aws.sdk.kotlin.services.ec2.waiters.waitUntilInstanceTerminated
import core.ClusterEngine
import core.ClusterEngineIO
import core.domain.ClustEngineInstance
import core.domain.IpRoutingMode
import core.domain.KeyAuth
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.util.ScriptConfigLoader
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

private val logger: Logger = LoggerFactory.getLogger("AWSClusterEngine")

class AWSClusterEngine(
    val fetchExecutionTimes: Boolean,
    val client: Ec2Client,
    val defaultInstanceType: InstanceType,
    val clusterSize: Int,
    val clusterKey: KeyAuth,
    val clusterName: String,
    val subnetID: String,
    val securityGroup: String,
    val imageId: String,
    ipRoutingMode: IpRoutingMode,
    io: ClusterEngineIO,
) : ClusterEngine<Instance>(ipRoutingMode, io) {
    override suspend fun isClusterInited(io: ClusterEngineIO): Boolean {
        val currentInstances = getClusterInstances(io)
        // If there is at least one instance matching the name, the cluster is initiated.
        return currentInstances.isNotEmpty()
    }

    override suspend fun getClusterInstances(io: ClusterEngineIO): List<ClustEngineInstance> {
        val response = client.describeInstances { }

        val instances =
            response.reservations
                ?.flatMap { it.instances ?: emptyList() }
                ?.filter { instance ->
                    val nameTag = instance.tags?.find { it.key == "Name" }?.value ?: ""
                    val stateName = instance.state?.name?.value
                    (stateName == "running" || stateName == "stopped" || stateName == "pending" || stateName == "stopping") &&
                        nameTag.startsWith("$clusterName-")
                } ?: emptyList()

        return mapInstancesToClustEngine(instances)
    }

    override suspend fun createCluster(
        io: ClusterEngineIO,
        ipRoutingMode: IpRoutingMode,
    ): Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>> {
        val request =
            RunInstancesRequest {
                imageId = this@AWSClusterEngine.imageId

                instanceType = defaultInstanceType

                keyName = "servers"
                /*
                            The maximum number of instances to launch.
                            If you specify a value that is more capacity than Amazon EC2 can launch in the target Availability Zone,
                            Amazon EC2 launches the largest possible number of instances above the specified minimum count.
                 */
                maxCount = clusterSize
                /*
                          The minimum number of instances to launch.
                          If you specify a value that is more capacity than Amazon EC2 can provide in the target Availability Zone,
                          Amazon EC2 does not launch any instances.
                 */
                minCount = clusterSize
                subnetId = subnetID
                securityGroupIds = listOf(securityGroup)
            }

        val response = client.runInstances(request)
        val instances = response.instances.orEmpty()

        check(instances.size >= clusterSize) {
            "Expected at least $clusterSize instances, but got ${instances.size}"
        }

        try {
            withTimeout(5.minutes) {
                client.waitUntilInstanceStatusOk {
                    instanceIds = instances.mapNotNull { it.instanceId }
                }
            }
        } catch (e: TimeoutCancellationException) {
            logger.error("Timed out waiting for EC2 instances to become status OK: ${e.message}")
        }

        val updatedInstances =
            client.describeInstances {
                instanceIds = instances.mapNotNull { it.instanceId }
            }.reservations?.flatMap { it.instances ?: emptyList() } ?: emptyList()

        for ((index, instance) in updatedInstances.withIndex()) {
            val machineName = "$clusterName-${index + 1}"
            val instanceId = instance.instanceId

            val tag =
                Tag {
                    key = "Name"
                    value = machineName
                }

            val requestTags =
                CreateTagsRequest {
                    resources = listOf(instanceId.toString())
                    tags = listOf(tag)
                }

            client.createTags(requestTags)
        }

        val primaryInstance = updatedInstances.first().mapInstanceToClustEngine()

        val clusterEngineInstances = mapInstancesToClustEngine(updatedInstances)

        return buildCluster(
            primaryInstance = primaryInstance,
            instances = clusterEngineInstances,
            clusterKeyFilePath = clusterKey.keyFilePath,
            clusterSize = clusterSize,
            clusterName = clusterName,
            scriptPath = ScriptConfigLoader.SCRIPT_PATH,
            rebootInstance = { instanceId ->
                client.rebootInstances { instanceIds = listOf(instanceId) }
            },
            waitUntilInstanceRunning = { isPrimary ->
                val targetIds =
                    if (isPrimary) {
                        listOf(primaryInstance.id)
                    } else {
                        clusterEngineInstances
                            .filter { it.id != primaryInstance.id }
                            .map { it.id }
                    }
                if (targetIds.isNotEmpty()) {
                    client.waitUntilInstanceStatusOk {
                        instanceIds = targetIds
                    }
                }
            },
            instanceTypeId = defaultInstanceType.value,
            fetchExecutionTimes = fetchExecutionTimes,
        )
    }

    override suspend fun startInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val targetIds = if (id != null) listOf(id) else getClusterInstances(io).map { it.id }
        if (targetIds.isEmpty()) return

        val request =
            StartInstancesRequest {
                instanceIds = targetIds
            }
        client.startInstances(request)
    }

    override suspend fun stopInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val targetIds = if (id != null) listOf(id) else getClusterInstances(io).map { it.id }
        if (targetIds.isEmpty()) return

        val request =
            StopInstancesRequest {
                instanceIds = targetIds
            }
        client.stopInstances(request)
    }

    override suspend fun terminateClusterOrInstance(
        io: ClusterEngineIO,
        id: String?,
    ) {
        val targetIds = if (id != null) listOf(id) else getClusterInstances(io).map { it.id }
        if (targetIds.isEmpty()) {
            logger.warn("No instances found to terminate.")
            return
        }

        logger.info("Terminating instances: $targetIds")
        client.terminateInstances {
            instanceIds = targetIds
        }

        try {
            withTimeout(5.minutes) {
                client.waitUntilInstanceTerminated {
                    instanceIds = targetIds
                }
            }
            logger.info("Successfully terminated instances: $targetIds")

            // deleteCluster(io, clusterName)
        } catch (e: TimeoutCancellationException) {
            logger.error("Timed out waiting for EC2 instances to terminate: ${e.message}")
        }
    }

    override fun mapInstancesToClustEngine(instancesToMap: List<Instance>): List<ClustEngineInstance> =
        instancesToMap.map { it.mapInstanceToClustEngine() }

    override fun Instance.mapInstanceToClustEngine(): ClustEngineInstance {
        return ClustEngineInstance(
            id = this.instanceId ?: "",
            privateIpAddress = this.privateIpAddress ?: "",
            publicDns = this.publicDnsName ?: "",
            publicIp = this.publicIpAddress ?: "",
        )
    }
}
