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
import tui.ConsoleApp

fun main() {
    try {
        val clusterSize = ConfigurationsLoader("config/config.properties").getProperty("cluster.size").toInt()

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
        val engine =
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
                clusterSize = clusterSize,
                clusterKey =
                    KeyAuth(
                        fileName = "servers-fct.pem",
                        // keyName = "servers-fct",
                        // keyFilePath = "C:\\Users\\berna\\Desktop\\docs\\servers-fct.pem",
                        keyFilePath = "/home/bernardo/.ssh/servers-fct.pem",
                    ),
                clusterName = "ABC",
                ipRoutingMode = SingleIpRouting(),
                auth = auth,
                io = io,
            )

        val service = ClusterService(engine, io)

        val app = ConsoleApp(service)
        app.run()
    } catch (_: Exception) {
    } finally {
        java.io.File("temp").deleteRecursively()
        java.io.File("tmp").deleteRecursively()
    }
}
