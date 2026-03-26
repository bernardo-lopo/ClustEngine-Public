package core

import core.domain.ClustEngineInstance
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.util.HostFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

const val SCRIPT_PATH = "scripts/setup_cluster.sh"
const val SCRIPT_NAME = "setup_cluster.sh"

private val logger: Logger = LoggerFactory.getLogger("ClusterEngineIO")

const val TERMINATION_STRING = "kill-process"

const val PATH_TO_KNOWN_HOSTS_FILE = "tmp/tmp_hostfile"

class ClusterEngineIO {
    private val hostFileManager = HostFileManager()
    private var orderToFinishProcess: Continuation<Unit>? = null

    suspend fun prepareSshPipeline(
        primaryInstancePublicIp: String,
        userOnInstance: String = "ubuntu",
        scriptPath: String,
        clusterKeyFilePath: String,
        primaryInstance: ClustEngineInstance,
        allInstances: List<ClustEngineInstance>,
        onPipelineReady: () -> Unit = {},
        instanceIdxObj: AtomicInteger,
    ) {
        if (!scriptExists(scriptPath)) return

        val orderedInstances = listOf(primaryInstance) + allInstances.filter { it.id != primaryInstance.id }

        val portForwardingElements =
            orderedInstances.flatMap { instance ->
                listOf("-L", "${instanceIdxObj.getAndIncrement()}:${instance.privateIpAddress}:22")
            }

        instanceIdxObj.set(START_INSTANCE_IDX)

        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()

        val pathToKnownHostsFile = "tmp/tmp_hostfile_${instanceIdxObj.get()}"

        val commandToCheck =
            arrayOf(
                "ssh",
//                "-N", // Does not run a shell.
//                "-T", // Does not allocate a terminal
                "-o", "ExitOnForwardFailure=yes",
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=$pathToKnownHostsFile",
                "-i",
                clusterKeyFilePath,
                "$userOnInstance@$primaryInstancePublicIp",
                "exit",
            )

        while (true) {
            val process =
                withContext(Dispatchers.IO) {
                    startProcess(commandToCheck).start()
                }

            while (process.isAlive)
                Thread.yield()

            if (process.exitValue() == 0) break

            println(process.errorStream.bufferedReader().readText())
        }

        val baseCommandSsh =
            arrayOf(
                "ssh",
                "-t",
                "-t",
                "-o", "UserKnownHostsFile=$pathToKnownHostsFile",
                "-o", "ExitOnForwardFailure=yes",
                "-o", "LogLevel=ERROR",
                "-o", "StrictHostKeyChecking=no",
                "-i",
                clusterKeyFilePath,
            ).plus(portForwardingElements.toTypedArray()).plus(
                arrayOf(
                    "$userOnInstance@$primaryInstancePublicIp",
                ),
            )

        val process =
            withContext(Dispatchers.IO) {
                startProcess(baseCommandSsh).start()
            }

        // Indicates when the ssh connections is established
        if (withContext(Dispatchers.IO) {
                process.inputStream.read()
            } == -1
        ) {
            throw Exception("Erro")
        }

        logger.info("!!!! WAITING FOR THE DELAY !!!!")

        onPipelineReady()
        /*
            After the process is being started it is launchd a suspendCancellableCoroutine.
            The execution is suspended after the lambda, keeping the SSH process running in the background.
         */
        suspendCancellableCoroutine { continuation ->
            // The continuation is stored in the shared mutable state orderToFinishProcess.
            // This is being done to schedule the close of the ssh pipeline.
            orderToFinishProcess = continuation
            // If by any reason the coroutine is cancel it is mandatory to terminate the process with flag -9
            continuation.invokeOnCancellation {
                process.destroyForcibly()
            }
        }
        // This code will be executed when the coroutine is resumed
        if (process.isAlive) {
            process.destroy()
        }
    }

    fun closeSshPipeline() {
        // When the function is called it is created a snapshot of the shared-mutable state
        val continuation = orderToFinishProcess
        if (continuation != null) {
            orderToFinishProcess = null
            logger.info("Signal received")
            logger.info("!!!!!!!PIPELINE CLOSED!!!!!!")
            continuation.resume(Unit)
        } else {
            logger.warn("The pipeline is not active")
        }
    }

    fun runScriptOnInstance(
        primaryInstanceIP: String,
        userOnInstance: String = "ubuntu",
        publicIp: String,
        isPrimary: Boolean,
        scriptPath: String,
        clusterKeyFilePath: String,
        allInstances: List<ClustEngineInstance>,
    ): Boolean {
        if (!scriptExists(scriptPath)) return false

        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()

        val baseCommandScp =
            arrayOf(
                "scp",
                // This option accepts fingerprint and adds to the known hosts file.
                "-o", "StrictHostKeyChecking=no",
                // This option sets the timeout for the connection in seconds.
                "-o", "ConnectTimeout=10",
                "-o", "UserKnownHostsFile=$PATH_TO_KNOWN_HOSTS_FILE",
                "-i", clusterKeyFilePath,
                SCRIPT_PATH,
                "$userOnInstance@$publicIp:",
            )

        baseCommandScp.forEach {
            print("$it ")
        }

        if (!executeSecureShellCommand(baseCommandScp, isScp = true)) return false

        val clusterPrivateIps = allInstances.joinToString(" ") { it.privateIpAddress }
        val extraArgs = if (isPrimary) arrayOf("-p", "-s", primaryInstanceIP) else arrayOf("-s", primaryInstanceIP)

        // It is build an SSH command based if it's the primary instance or not.
        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()
        val baseCommand =
            arrayOf(
                "ssh",
                // This option accepts fingerprint and adds to the known hosts file.
                "-o",
                "StrictHostKeyChecking=no",
                // This option sets the timeout for the connection in seconds.
                "-o", "ConnectTimeout=10",
                "-o", "UserKnownHostsFile=$PATH_TO_KNOWN_HOSTS_FILE",
                "-i",
                clusterKeyFilePath,
                "$userOnInstance@$publicIp",
            )
        return allowExecutionAndRun(userOnInstance, baseCommand, extraArgs, clusterPrivateIps)
    }

    fun runScriptOnInstanceUsingOnePublicIp(
        primaryInstancePrivateIP: String,
        userOnInstance: String = "ubuntu",
        isPrimary: Boolean,
        clusterKeyFilePath: String,
        allInstances: List<ClustEngineInstance>,
        instanceIdx: Int,
    ): Boolean {
        val clusterPrivateIps = allInstances.joinToString(" ") { it.privateIpAddress }

        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()

        val hostFileManager = HostFileManager(instanceIdx.toString())
        hostFileManager.deleteHostFile()
        val pathToKnownHostsFile = hostFileManager.createHostFile()

        val baseCommandScp =
            arrayOf(
                "scp",
                // This option accepts fingerprint and adds to the known hosts file.
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=$pathToKnownHostsFile",
                // This option sets the timeout for the connection in seconds.
                "-i", clusterKeyFilePath,
                "-P",
                instanceIdx.toString(),
                SCRIPT_PATH,
                "$userOnInstance@localhost:",
            )

        baseCommandScp.forEach {
            print("$it ")
        }

        if (!executeSecureShellCommand(baseCommandScp, isScp = true)) return false

        val extraArgs =
            if (isPrimary) arrayOf("-p", "-s", primaryInstancePrivateIP) else arrayOf("-s", primaryInstancePrivateIP)

        // It is build an SSH command based if it's the primary instance or not.
        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()
        val baseCommandSsh =
            arrayOf(
                "ssh",
                // This option accepts fingerprint and adds to the known hosts file.
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=$pathToKnownHostsFile",
                // This option sets the timeout for the connection in seconds.
                "-o",
                "ConnectTimeout=10",
                "-i",
                clusterKeyFilePath,
                "-p",
                instanceIdx.toString(),
                "$userOnInstance@localhost",
            )

        return allowExecutionAndRun(
            userOnInstance,
            baseCommandSsh,
            extraArgs,
            clusterPrivateIps,
        )
    }

    fun getTimesFromMachineUsingOnePublicIp(
        isPrimary: Boolean,
        clusterKeyFilePath: String,
        userOnInstance: String = "ubuntu",
        instanceIdxObj: AtomicInteger,
    ): Any {
        val tempDirPath = "temp"
        val port = instanceIdxObj.get()
        val tempFilePath = "$tempDirPath/temp_data_localport_$port.txt"

        try {
            Files.createDirectories(Paths.get(tempDirPath))

            hostFileManager.deleteHostFile()
            hostFileManager.createHostFile()

            val pathToKnownHostsFile = "tmp/tmp_hostfile_${instanceIdxObj.get()}"

            val copyTimesCommand =
                arrayOf(
                    "scp",
                    "-o", "StrictHostKeyChecking=no",
                    "-o", "ConnectTimeout=10",
                    "-o", "UserKnownHostsFile=$pathToKnownHostsFile",
                    "-i", clusterKeyFilePath,
                    "-P", port.toString(),
                    "$userOnInstance@localhost:/home/ubuntu/times.txt",
                    tempFilePath,
                )

            // Executes the SCP command
            val success = executeSecureShellCommand(copyTimesCommand, isScp = true)

            if (!success) {
                logger.warn("Could not retrieve times.txt from instance via port $port.")
                return timesFromFileContent(isPrimary, null)
            }

            // Verify if the file was actually created and has content
            val downloadedFile = File(tempFilePath)
            if (!downloadedFile.exists() || downloadedFile.length() == 0L) {
                logger.warn("File times.txt was not downloaded correctly or is empty for port $port.")
                return timesFromFileContent(isPrimary, null)
            }

            // Reads the contents of the file
            val lines = downloadedFile.readLines()

            // Deletes the temporary file after reading to clean up
            downloadedFile.delete()

            // Warns if the file has fewer lines than expected
            if (isPrimary && lines.size < 5) {
                logger.warn("Primary times.txt on port $port has less than 5 lines: $lines")
            } else if (!isPrimary && lines.size < 3) {
                logger.warn("Non-primary times.txt on port $port has less than 3 lines: $lines")
            }

            // Returns the appropriate data object with fallback to "error" if lines are missing
            return timesFromFileContent(isPrimary, lines)
        } catch (e: Exception) {
            logger.error("Failed to get times from instance via port ${instanceIdxObj.get()}: ${e.message}")
            return timesFromFileContent(isPrimary, null)
        }
    }

    fun getTimesFromMachine(
        isPrimary: Boolean,
        clusterKeyFilePath: String,
        userOnInstance: String = "ubuntu",
        publicIp: String,
        instanceIdxObj: AtomicInteger,
    ): Any {
        val tempDirPath = "temp"
        val tempFilePath = "$tempDirPath/temp_data_$publicIp.txt"

        return try {
            // Creates the temporary directory to store the copied file
            Files.createDirectories(Paths.get(tempDirPath))

            // Builds the SCP command to copy the times.txt file from the instance
            hostFileManager.deleteHostFile()
            hostFileManager.createHostFile()

            val pathToKnownHostsFile = "tmp/tmp_hostfile_${instanceIdxObj.get()}"

            val copyTimesCommand =
                arrayOf(
                    "scp",
                    "-o", "StrictHostKeyChecking=no",
                    "-o", "ConnectTimeout=10",
                    "-o", "UserKnownHostsFile=$pathToKnownHostsFile",
                    "-i", clusterKeyFilePath,
                    "$userOnInstance@$publicIp:/home/ubuntu/times.txt",
                    tempFilePath,
                )

            // Executes the SCP command
            val success = executeSecureShellCommand(copyTimesCommand, isScp = true)
            if (!success) {
                logger.warn("Could not retrieve times.txt from instance $publicIp.")
                return timesFromFileContent(isPrimary, null)
            }

            // Reads the contents of the file
            val lines = File(tempFilePath).readLines()

            // Deletes the temporary file after reading
            File(tempFilePath).delete()

            // Warns if the file has fewer lines than expected
            if (isPrimary && lines.size < 5) {
                logger.warn("Primary times.txt on $publicIp has less than 5 lines: $lines")
            } else if (!isPrimary && lines.size < 3) {
                logger.warn("Non-primary times.txt on $publicIp has less than 3 lines: $lines")
            }

            // Returns the appropriate data object with fallback to "error" if lines are missing
            timesFromFileContent(isPrimary, lines)
        } catch (e: Exception) {
            logger.error("Failed to get times from instance $publicIp: ${e.message}")
            if (isPrimary) {
                PartialTimesPrimary("error", "error", "error", "error", "error")
            } else {
                PartialTimesNonPrimary("error", "error", "error")
            }
        }
    }
}

private fun scriptExists(scriptPath: String): Boolean {
    try {
        // Create a File object for the script
        val scriptFile = File(scriptPath)

        if (!scriptFile.exists()) {
            logger.error("Script file not found at the provided path: $scriptPath")
            return false
        }

        return true
    } catch (e: Exception) {
        logger.error("Error while creating File object for the script: $scriptPath: ${e.message}")
        return false
    }
}

private fun allowExecutionAndRun(
    userOnInstance: String,
    baseCommand: Array<String>,
    extraArgs: Array<String>,
    clusterPrivateIps: String,
): Boolean {
    val remoteScriptPath = "/home/$userOnInstance/$SCRIPT_NAME"

    val sshCommandScript =
        arrayOf("chmod", "+x", "$remoteScriptPath;") + arrayOf(remoteScriptPath) + extraArgs + clusterPrivateIps

    val sshCommand = baseCommand + arrayOf(sshCommandScript.joinToString(" "))

    return executeSecureShellCommand(sshCommand, isScp = false)
}

// This function executes a remote secure shell command, such as ssh or scp
// and returns true in case of success or false, otherwise.
fun executeSecureShellCommand(
    command: Array<String>,
    isScp: Boolean,
): Boolean {
    try {
        val process = startProcess(command).start()

        logger.info("Waiting the command ${command[0]}...")

        if (isScp) {
            process.waitFor()
            if (process.exitValue() != 0) {
                logger.error("SCP command failed with exit code: ${process.exitValue()}")
                logger.error(process.errorStream.bufferedReader().readText())
                return false
            }
            return true
        }

        val found: Boolean =
            try {
                process.inputStream.bufferedReader().use { reader ->
                    // It creates a lazy sequence
                    reader.lineSequence()
                        .onEach { line ->
                            logger.info("OUT: $line")
                        }
                        .any { line ->
                            // The process will stop immediately when the string is found
                            line.contentEquals(TERMINATION_STRING)
                        }
                }
            } catch (e: IOException) {
                logger.error("Error reading process output: ${e.message}")
                false
            }

        if (found) {
            logger.info("Termination string was found... terminating")
            // The string was found, the process will be killed
            process.destroy()
        } else {
            logger.warn("it was not found the termination string")
        }
    } catch (e: Exception) {
        logger.error("Command execution failed: ${e.message}")
        return false
    }
    return true
}

private fun startProcess(
    command: Array<String>,
    redirectError: Boolean = true,
    path: String = ":/usr/bin:/bin",
): ProcessBuilder {
    // Start the process and joins the stderr with stdout
    val processBuilder =
        // The * is used to convert an array into varargs
        ProcessBuilder(*command)
            .redirectErrorStream(redirectError)

    if (!System.getProperty("os.name").lowercase().contains("win")) {
        val env = processBuilder.environment()
        env["PATH"] = env["PATH"] + path
    }

    logger.info("Starting command: '${command.joinToString(" ")}'...")
    return processBuilder
}

private fun timesFromFileContent(
    isPrimary: Boolean,
    fileContent: List<String>?,
): Any {
    return if (isPrimary) {
        PartialTimesPrimary(
            dependenciesUpdate = fileContent?.getOrNull(0) ?: "error",
            dependenciesInstall = fileContent?.getOrNull(1) ?: "error",
            nfsServerInstallAndConfig = fileContent?.getOrNull(2) ?: "error",
            mpiDownload = fileContent?.getOrNull(3) ?: "error",
            mpiConfigCompile = fileContent?.getOrNull(4) ?: "error",
        )
    } else {
        PartialTimesNonPrimary(
            dependenciesUpdate = fileContent?.getOrNull(0) ?: "error",
            dependenciesInstall = fileContent?.getOrNull(1) ?: "error",
            nfsClientInstall = fileContent?.getOrNull(2) ?: "error",
        )
    }
}
