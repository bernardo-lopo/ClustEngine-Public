package apps

import apps.ClusterEngineFactoryLoader.createAWSService
import apps.ClusterEngineFactoryLoader.createOpenStackService
import core.domain.MultiIpRouting
import core.domain.SingleIpRouting
import tui.ConsoleApp
import tui.ServiceSelection
import java.io.File
import kotlin.system.exitProcess

const val COROUTINE_PROPERTY = "kotlinx.coroutines.io.parallelism"
const val MAX_COROUTINES = "512"

fun main() {
    System.setProperty(COROUTINE_PROPERTY, MAX_COROUTINES)

    File("tmp").deleteRecursively()

    println("=== Cluster Configuration ===")
    print("Name the Cluster (ex: ABC): ")
    val clusterName = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "DefaultCluster"

    print("Set the number of instances or default to 2: ")
    val clusterSize = readlnOrNull()?.toIntOrNull() ?: 2
    println("===============================\n")

    val selectedService = ServiceSelection()
    val userChoice = selectedService.selectService()

    val app: ConsoleApp =
        when (userChoice) {
            ServiceSelection.ServiceOption.AWS -> {
                val awsService =
                    createAWSService(
                        clusterName,
                        clusterSize,
                        instanceTypeId = "t2.micro",
                        ipRoutingMode = MultiIpRouting(),
                    )
                ConsoleApp(awsService)
            }

            ServiceSelection.ServiceOption.OpenStack -> {
                val openStackService =
                    createOpenStackService(
                        clusterName,
                        clusterSize,
                        instanceTypeId = "20a6fa2c-6e11-4293-b3e9-4cae46b69a64",
                        ipRoutingMode = SingleIpRouting(),
                    )
                ConsoleApp(openStackService)
            }

            ServiceSelection.ServiceOption.Exit -> exitProcess(0)
            else -> throw Exception("Invalid user choice")
        }
    app.run()
}
