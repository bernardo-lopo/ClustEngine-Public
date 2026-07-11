package tui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger: Logger = LoggerFactory.getLogger("SelectionService")

class ServiceSelection {
    enum class ServiceOption {
        Exit,
        AWS,
        OpenStack,
        Unknown,
    }

    fun selectService(): ServiceOption {
        while (true) {
            var userInput: ServiceOption
            do {
                clearConsole()
                userInput = displayMenu()
                try {
                    when (userInput) {
                        ServiceOption.Exit -> {
                            logger.info("User requested exit, shutting down ...")
                            exitProcess(0)
                        }
                        ServiceOption.AWS -> return ServiceOption.AWS
                        ServiceOption.OpenStack -> return ServiceOption.OpenStack
                        ServiceOption.Unknown -> println("Invalid option")
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
            } while (userInput != ServiceOption.Exit)
        }
    }

    private fun displayMenu(): ServiceOption {
        var serviceOption = ServiceOption.Unknown
        try {
            clearConsole()
            println()
            println("0. Exit")
            println("1. AWS")
            println("2. OpenStack")
            println()
            print("> ")
            val scanner = readln().toInt()
            serviceOption =
                if (scanner >= ServiceOption.entries.size) {
                    ServiceOption.Unknown
                } else {
                    ServiceOption.entries.toTypedArray()[scanner]
                }
        } catch (_: NumberFormatException) {
            println("Invalid option")
        } catch (_: NullPointerException) {
            println("Invalid option")
            displayMenu()
        } catch (e: Exception) {
            clearConsole()
            println("Contact the dev team")
            println(e.message)
            exitProcess(2)
        }
        return serviceOption
    }
}
