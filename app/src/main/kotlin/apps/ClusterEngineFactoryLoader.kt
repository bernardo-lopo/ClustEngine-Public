package apps

import AWSClusterEngine
import OpenStackClusterEngine
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.model.InstanceType
import client.OpenStackExecuteRequest
import client.OpenStackRequestMappingSdk
import client.OpenStackSdk
import core.ClusterEngineFactoryInterface
import core.ClusterEngineIO
import core.ClusterService
import core.ClusterServiceProviderInterface
import core.domain.IpRoutingMode
import core.domain.KeyAuth
import core.domain.LiveCluster
import core.domain.MultiIpRouting
import core.domain.SingleIpRouting
import core.util.ScriptConfigLoader
import dto.auth.Auth
import dto.auth.Domain
import dto.auth.Identity
import dto.auth.Password
import dto.auth.Project
import dto.auth.Scope
import dto.auth.User
import java.io.File

object ClusterEngineFactoryLoader : ClusterEngineFactoryInterface {
    override fun createFromSavedCluster(cluster: LiveCluster): ClusterServiceProviderInterface {
        val savedFlavor =
            cluster.instanceTypeId
                ?: if (cluster.provider == "AWSClusterEngine") "t2.micro" else "20a6fa2c-6e11-4293-b3e9-4cae46b69a64"

        return when (cluster.provider) {
            "AWSClusterEngine" ->
                createAWSService(
                    cluster.clusterName,
                    cluster.clusterSize,
                    savedFlavor,
                    MultiIpRouting(),
                )

            "OpenStackClusterEngine" ->
                createOpenStackService(
                    cluster.clusterName,
                    cluster.clusterSize,
                    savedFlavor,
                    SingleIpRouting(),
                )

            else -> throw IllegalArgumentException("Unknown cloud provider saved in JSON: ${cluster.provider}")
        }
    }

    override fun createAWSService(
        clusterName: String,
        clusterSize: Int,
        instanceTypeId: String,
        ipRoutingMode: IpRoutingMode,
    ): ClusterServiceProviderInterface {
        val awsClient = Ec2Client.Companion { region = ScriptConfigLoader.getEnvOrThrow("AWS_CLIENT_REGION") }
        val awsIO = ClusterEngineIO()

        val awsEngine =
            AWSClusterEngine(
                client = awsClient,
                defaultInstanceType = InstanceType.fromValue(instanceTypeId),
                clusterSize = clusterSize,
                clusterKey =
                    KeyAuth(
                        fileName = File(ScriptConfigLoader.getEnvOrThrow("AWS_KEY_FILE_PATH")).name,
                        keyFilePath = ScriptConfigLoader.getEnvOrThrow("AWS_KEY_FILE_PATH"),
                    ),
                clusterName = clusterName,
                subnetID = ScriptConfigLoader.getEnvOrThrow("AWS_SUBNET_ID"),
                securityGroup = ScriptConfigLoader.getEnvOrThrow("AWS_SECURITY_GROUP_ID"),
                imageId = ScriptConfigLoader.getEnvOrThrow("AWS_IMAGE_ID"),
                ipRoutingMode = ipRoutingMode,
                io = awsIO,
                fetchExecutionTimes = false,
            )
        return ClusterService(awsEngine, awsIO)
    }

    override fun createOpenStackService(
        clusterName: String,
        clusterSize: Int,
        instanceTypeId: String,
        ipRoutingMode: IpRoutingMode,
    ): ClusterServiceProviderInterface {
        val auth =
            Auth(
                identity =
                    Identity(
                        methods = listOf("password"),
                        password =
                            Password(
                                user =
                                    User(
                                        name = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_USER_NAME"),
                                        domain = Domain(ScriptConfigLoader.getEnvOrThrow("OPENSTACK_DOMAIN")),
                                        password = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_PASSWORD"),
                                    ),
                            ),
                    ),
                scope =
                    Scope(
                        project =
                            Project(
                                name = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_PROJECT_NAME"),
                                domain = Domain(ScriptConfigLoader.getEnvOrThrow("OPENSTACK_DOMAIN")),
                            ),
                    ),
            )

        val openStackIO = ClusterEngineIO()
        val openStackEngine =
            OpenStackClusterEngine(
                openStackExecuteRequest =
                    OpenStackExecuteRequest(
                        openStack = OpenStackSdk(baseUrl = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_BASE_URL")),
                        openStackCloudRequests =
                            OpenStackRequestMappingSdk(
                                auth = auth,
                                flavorRefId = instanceTypeId,
                                imageRefId = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_IMAGE_ID"),
                                availabilityZone = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_AVAILABILITY_ZONE"),
                                securityGroup = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_SECURITY_GROUP"),
                                networkId = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_NETWORK_ID"),
                                privateKeyName = File(ScriptConfigLoader.getEnvOrThrow("OPENSTACK_KEY_FILE_PATH")).nameWithoutExtension,
                            ),
                    ),
                clusterSize = clusterSize,
                clusterKey =
                    KeyAuth(
                        fileName = File(ScriptConfigLoader.getEnvOrThrow("OPENSTACK_KEY_FILE_PATH")).nameWithoutExtension,
                        keyFilePath = ScriptConfigLoader.getEnvOrThrow("OPENSTACK_KEY_FILE_PATH"),
                    ),
                clusterName = clusterName,
                ipRoutingMode = ipRoutingMode,
                auth = auth,
                io = openStackIO,
                fetchExecutionTimes = false,
            )
        return ClusterService(openStackEngine, openStackIO)
    }
}
