package apps

import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.model.InstanceType
import core.ClusterEngineIO
import core.ClusterService
import core.domain.KeyAuth
import core.domain.MultiIpRouting
import core.util.ConfigurationsLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import providers.aws.AWSClusterEngine
import tester.Tester

fun main(): Unit =
    try {
        runBlocking(Dispatchers.IO) {
            val config = ConfigurationsLoader("config/aws_config.properties")

            val ec2Client = Ec2Client { region = "eu-central-1" }

            val io = ClusterEngineIO()

            val engine2 =
                AWSClusterEngine(
                    client = ec2Client,
                    defaultInstanceType = InstanceType.T2Micro,
                    clusterSize = 2,
                    clusterKey =
                        KeyAuth(
                            fileName = config.getProperty("key.file.name"),
                            keyFilePath = config.getProperty("key.file.path"),
                        ),
                    clusterName = "ABC",
                    subnetID = config.getProperty("subnet.id"),
                    securityGroup = config.getProperty("securitygroup.id"),
                    imageId = config.getProperty("default.os"),
                    ipRoutingMode = MultiIpRouting(),
                    io = io,
                )

            val engine4 =
                AWSClusterEngine(
                    client = ec2Client,
                    defaultInstanceType = InstanceType.T2Micro,
                    clusterSize = 4,
                    clusterKey =
                        KeyAuth(
                            fileName = config.getProperty("key.file.name"),
                            keyFilePath = config.getProperty("key.file.path"),
                        ),
                    clusterName = "ABC",
                    subnetID = config.getProperty("subnet.id"),
                    securityGroup = config.getProperty("securitygroup.id"),
                    imageId = config.getProperty("default.os"),
                    ipRoutingMode = MultiIpRouting(),
                    io = io,
                )

            val engine8 =
                AWSClusterEngine(
                    client = ec2Client,
                    defaultInstanceType = InstanceType.T2Micro,
                    clusterSize = 8,
                    clusterKey =
                        KeyAuth(
                            fileName = config.getProperty("key.file.name"),
                            keyFilePath = config.getProperty("key.file.path"),
                        ),
                    clusterName = "ABC",
                    subnetID = config.getProperty("subnet.id"),
                    securityGroup = config.getProperty("securitygroup.id"),
                    imageId = config.getProperty("default.os"),
                    ipRoutingMode = MultiIpRouting(),
                    io = io,
                )

            val service2 = ClusterService(engine2, io)
            val service4 = ClusterService(engine4, io)
            val service8 = ClusterService(engine8, io)

            val test1 =
                Tester(
                    service2,
                    config = ConfigurationsLoader("config/config_tester.properties"),
                    nInstances = 2,
                )

            val test2 =
                Tester(
                    service4,
                    config = ConfigurationsLoader("config/config_tester.properties"),
                    nInstances = 4,
                )

            val test3 =
                Tester(
                    service8,
                    config = ConfigurationsLoader("config/config_tester.properties"),
                    nInstances = 8,
                )

            // test1.run()
            // test2.run()
            test3.run()
        }
    } catch (_: Exception) {
    } finally {
        java.io.File("temp").deleteRecursively()
        java.io.File("tmp").deleteRecursively()
    } as Unit
