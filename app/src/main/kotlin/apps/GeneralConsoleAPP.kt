package apps

import AWSClusterEngine
import OpenStackClusterEngine
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.model.InstanceType
import client.OpenStackExecuteRequest
import client.OpenStackRequestMappingSdk
import client.OpenStackSdk
import core.ClusterEngineIO
import core.ClusterService
import core.domain.KeyAuth
import core.domain.MultiIpRouting
import core.domain.SingleIpRouting
import core.util.ConfigurationsLoader
import dto.auth.Auth
import dto.auth.Domain
import dto.auth.Identity
import dto.auth.Password
import dto.auth.Project
import dto.auth.Scope
import dto.auth.User
import tui.ConsoleApp
import tui.ServiceSelection
import java.io.File

fun main() {
    try {
        File("tmp").deleteRecursively()

        val clusterSize = ConfigurationsLoader("config/config.properties").getProperty("cluster.size").toInt()
        val clusterName = ConfigurationsLoader("config/config.properties").getProperty("cluster.name")

        val openStackConfig = ConfigurationsLoader("config/openstack_credentials.properties")

        val auth =
            Auth(
                identity =
                    Identity(
                        methods = listOf("password"),
                        password =
                            Password(
                                user =
                                    User(
                                        name = openStackConfig.getProperty("openstack.user.name"),
                                        domain =
                                            Domain(
                                                openStackConfig.getProperty(
                                                    "openstack.domain",
                                                ),
                                            ),
                                        password = openStackConfig.getProperty("openstack.user.password"),
                                    ),
                            ),
                    ),
                scope =
                    Scope(
                        project =
                            Project(
                                name = openStackConfig.getProperty("openstack.project.name"),
                                domain =
                                    Domain(
                                        openStackConfig.getProperty(
                                            "openstack.domain",
                                        ),
                                    ),
                            ),
                    ),
            )

        val openStackIO = ClusterEngineIO()
        val openStackEngine =
            OpenStackClusterEngine(
                openStackExecuteRequest =
                    OpenStackExecuteRequest(
                        openStack =
                            OpenStackSdk(
                                baseUrl = "https://stratus.d.acnca.pt:8774/v2.1",
                            ),
                        openStackCloudRequests =
                            OpenStackRequestMappingSdk(
                                auth = auth,
                                flavorRefId = "20a6fa2c-6e11-4293-b3e9-4cae46b69a64",
                                imageRefId = "597fbc92-c33c-49f2-9793-d287b12690d9",
                                availabilityZone = "nova",
                                securityGroup = "default",
                                networkId = "4dfa5963-ece6-473f-be43-c0ab48dfc4af",
                                privateKeyName = "servers-fct",
                            ),
                    ),
                clusterSize = clusterSize,
                clusterKey =
                    KeyAuth(
                        fileName = "servers-fct.pem",
                        keyFilePath = "/home/bernardo/.ssh/servers-fct.pem",
                    ),
                clusterName = clusterName,
                ipRoutingMode = SingleIpRouting(),
                auth = auth,
                io = openStackIO,
                fetchExecutionTimes = false,
            )

        val openStackService = ClusterService(openStackEngine, openStackIO)

        val awsConfig = ConfigurationsLoader("config/aws_config.properties")

        val awsClient = Ec2Client { region = "eu-central-1" }

        val awsIO = ClusterEngineIO()

        val awsEngine =
            AWSClusterEngine(
                client = awsClient,
                defaultInstanceType = InstanceType.fromValue("t2.micro"),
                clusterSize = clusterSize,
                clusterKey =
                    KeyAuth(
                        fileName = awsConfig.getProperty("key.file.name"),
                        keyFilePath = awsConfig.getProperty("key.file.path"),
                    ),
                clusterName = clusterName,
                subnetID = awsConfig.getProperty("subnet.id"),
                securityGroup = awsConfig.getProperty("securitygroup.id"),
                imageId = awsConfig.getProperty("default.os"),
                ipRoutingMode = MultiIpRouting(),
                io = awsIO,
                fetchExecutionTimes = false,
            )

        val awsService = ClusterService(awsEngine, awsIO)

        val selectedService = ServiceSelection()

        val userChoice = selectedService.selectService()

        val app: ConsoleApp =
            when (userChoice) {
                ServiceSelection.ServiceOption.AWS -> ConsoleApp(awsService)
                ServiceSelection.ServiceOption.OpenStack -> ConsoleApp(openStackService)
                ServiceSelection.ServiceOption.Exit -> return
                else -> throw Exception("Invalid Selection")
            }

        app.run()
    } catch (_: Exception) {
    } finally {
        File("tmp").deleteRecursively()
    }
}
