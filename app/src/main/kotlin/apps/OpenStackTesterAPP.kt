package apps

import client.OpenStackExecuteRequest
import client.OpenStackRequestMappingSdk
import client.OpenStackSdk
import core.ClusterEngineIO
import core.ClusterService
import core.domain.KeyAuth
import core.domain.SingleIpRouting
import core.util.ConfigurationsLoader
import dto.auth.Auth
import dto.auth.Domain
import dto.auth.Identity
import dto.auth.Password
import dto.auth.Project
import dto.auth.Scope
import dto.auth.User
import providers.openstack.OpenStackClusterEngine
import tester.Tester

fun main() {
    try {
        val config = ConfigurationsLoader("config/openstack_credentials.properties")

        val auth =
            Auth(
                identity =
                    Identity(
                        methods = listOf("password"),
                        password =
                            Password(
                                user =
                                    User(
                                        name = config.getProperty("openstack.user.name"),
                                        domain =
                                            Domain(
                                                config.getProperty(
                                                    "openstack.domain",
                                                ),
                                            ),
                                        password = config.getProperty("openstack.user.password"),
                                    ),
                            ),
                    ),
                scope =
                    Scope(
                        project =
                            Project(
                                name = config.getProperty("openstack.project.name"),
                                domain =
                                    Domain(
                                        config.getProperty(
                                            "openstack.domain",
                                        ),
                                    ),
                            ),
                    ),
            )

        val io =
            ClusterEngineIO()
        val engine2 =
            OpenStackClusterEngine(
                openStackExecuteRequest =
                    OpenStackExecuteRequest(
                        openStack =
                            OpenStackSdk(
                                baseUrl = "https://stratus.d.acnca.pt:8774/v2.1",
                            ),
                        fctCloudRequests =
                            OpenStackRequestMappingSdk(
                                auth = auth,
                            ),
                    ),
                clusterSize = 2,
                clusterKey =
                    KeyAuth(
                        fileName = "servers-fct.pem",
                        // keyName = "servers-fct",
                        // keyFilePath = "C:\\Users\\berna\\Desktop\\docs\\servers-fct.pem",
                        keyFilePath = config.getProperty("openstack.user.keyfile-path"),
                    ),
                clusterName = "ABC",
                ipRoutingMode = SingleIpRouting(),
                auth = auth,
                io = io,
            )

        val engine4 =
            OpenStackClusterEngine(
                openStackExecuteRequest =
                    OpenStackExecuteRequest(
                        openStack =
                            OpenStackSdk(
                                baseUrl = "https://stratus.d.acnca.pt:8774/v2.1",
                            ),
                        fctCloudRequests =
                            OpenStackRequestMappingSdk(
                                auth = auth,
                            ),
                    ),
                clusterSize = 4,
                clusterKey =
                    KeyAuth(
                        fileName = "servers-fct.pem",
                        // keyName = "servers-fct",
                        // keyFilePath = "C:\\Users\\berna\\Desktop\\docs\\servers-fct.pem",
                        keyFilePath = config.getProperty("openstack.user.keyfile-path"),
                    ),
                clusterName = "ABC",
                ipRoutingMode = SingleIpRouting(),
                auth = auth,
                io = io,
            )

        val engine8 =
            OpenStackClusterEngine(
                openStackExecuteRequest =
                    OpenStackExecuteRequest(
                        openStack =
                            OpenStackSdk(
                                baseUrl = "https://stratus.d.acnca.pt:8774/v2.1",
                            ),
                        fctCloudRequests =
                            OpenStackRequestMappingSdk(
                                auth = auth,
                            ),
                    ),
                clusterSize = 8,
                clusterKey =
                    KeyAuth(
                        fileName = "servers-fct.pem",
                        // keyName = "servers-fct",
                        // keyFilePath = "C:\\Users\\berna\\Desktop\\docs\\servers-fct.pem",
                        keyFilePath = config.getProperty("openstack.user.keyfile-path"),
                    ),
                clusterName = "ABC",
                ipRoutingMode = SingleIpRouting(),
                auth = auth,
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
        test1.run()
        test2.run()
        test3.run()
    } catch (e: Exception) {
    } finally {
        java.io.File("temp").deleteRecursively()
        java.io.File("tmp").deleteRecursively()
    }
}
