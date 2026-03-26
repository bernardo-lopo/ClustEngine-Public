package tui

import core.ClusterManager
import core.ClusterServiceProviderInterface
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger: Logger = LoggerFactory.getLogger("ConsoleApp")

class ConsoleApp(service: ClusterServiceProviderInterface) : ClusterManager(service) {
    private enum class Option {
        Exit,
        InitCluster,
        StartCluster,
        StopCluster,
        DeleteCluster,
        StartById,
        StopById,
        DeleteInstance,
        Unknown,
    }

    fun run() {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("Shutdown initiated, closing...")
                safeExit()
            },
        )
        while (true) {
            var userInput: Option
            do {
                clearConsole()
                userInput = displayMenu()
                try {
                    when (userInput) {
                        Option.Exit -> {
                            logger.info("User requested exit, shutting down ...")
                            safeExit()
                            exitProcess(0)
                        }
                        Option.InitCluster -> {
                            val startTime = System.currentTimeMillis()
                            initCluster
                            val timeElapsed = System.currentTimeMillis() - startTime
                            logger.info("Total time: ${timeElapsed / 1000} seconds")
                        }

                        Option.StartCluster -> start
                        Option.StopCluster -> stop
                        Option.DeleteCluster -> {
                            deleteCluster
                            initCluster
                        }

                        Option.StartById -> {
                            val id = getId().toString()
                            startById(id)
                        }

                        Option.StopById -> {
                            val id = getId()
                            stopById(id.toString())
                        }

                        Option.DeleteInstance -> {
                            val id = getId()
                            deleteInstance(id.toString())
                        }

                        Option.Unknown -> println("Invalid option")
                    }
                } catch (_: NullPointerException) {
                    println("Invalid option")
                    displayMenu()
                } catch (e: Exception) {
                    clearConsole()
                    println("Contact the dev team")
                    println(e.message)
                    exitProcess(2)
                }
            } while (userInput != Option.Exit)
        }
    }

    private fun getId(): Int? {
        var id: Int? = null
        try {
            do {
                clearConsole()
                println("Enter instance ID:")
                id = readln().toIntOrNull()
            } while (id == null)
        } catch (_: RuntimeException) {
            println("Invalid ID")
            getId()
        }
        return id
    }

    private fun displayMenu(): Option {
        var option = Option.Unknown
        try {
            clearConsole()
            println(clustEngine())
            println()
            println("0. Exit")
            println("1. Init Cluster")
            println("2. Start Cluster")
            println("3. Stop Cluster")
            println("4. Delete Cluster")
            println("5. Start Instance")
            println("6. Stop Instance")
            println("7. Delete Instance")
            println()
            print("> ")
            val scanner = readln().toInt()
            option =
                if (scanner >= Option.entries.size) {
                    Option.Unknown
                } else {
                    Option.entries.toTypedArray()[scanner]
                }
        } catch (_: NullPointerException) {
            println("Invalid option")
            displayMenu()
        } catch (e: Exception) {
            clearConsole()
            println("Contact the dev team")
            println(e.message)
            exitProcess(2)
        }
        return option
    }

    private fun clearConsole() {
        // This console is 80 columns and 25 lines.
        repeat(25) { println("\n") }
    }

    private fun safeExit() {
        try {
            if (isInited) {
                logger.info("Cleaning up cluster before shutdown...")
                deleteCluster
                logger.info("Stopped using shutdown hook")
            }
        } catch (e: Exception) {
            logger.error("Error while shutting down", e)
        } finally {
            logger.info("Shutdown hook finished")
        }
    }

    fun clustEngine() =
        "+--------------------------------------------------------------------------------------------+\n" +
            "|                                                                                            |\n" +
            "|  ██████╗██╗     ██╗   ██╗███████╗████████╗███████╗███╗   ██╗ ██████╗ ██╗███╗   ██╗███████╗ |\n" +
            "| ██╔════╝██║     ██║   ██║██╔════╝╚══██╔══╝██╔════╝████╗  ██║██╔════╝ ██║████╗  ██║██╔════╝ |\n" +
            "| ██║     ██║     ██║   ██║███████╗   ██║   █████╗  ██╔██╗ ██║██║  ███╗██║██╔██╗ ██║█████╗   |\n" +
            "| ██║     ██║     ██║   ██║╚════██║   ██║   ██╔══╝  ██║╚██╗██║██║   ██║██║██║╚██╗██║██╔══╝   |\n" +
            "| ╚██████╗███████╗╚██████╔╝███████║   ██║   ███████╗██║ ╚████║╚██████╔╝██║██║ ╚████║███████╗ |\n" +
            "|  ╚═════╝╚══════╝ ╚═════╝ ╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝╚═╝  ╚═══╝╚══════╝ |\n" +
            "|                                                                                            |\n" +
            "|                                                                                            |\n" +
            "+--------------------------------------------------------------------------------------------+"
}
