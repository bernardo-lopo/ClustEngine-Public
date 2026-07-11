package core

import core.domain.ClustEngineInstance
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.util.HostFileManager
import core.util.ScriptConfigLoader.SCRIPT_NAME
import core.util.ScriptConfigLoader.SCRIPT_PATH
import core.util.ScriptConfigLoader.USER_SCRIPT_PATH
import core.util.SshCommandBuilder
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

private val logger: Logger = LoggerFactory.getLogger("ClusterEngineIO")
const val TERMINATION_STRING = "kill-process"
const val PATH_TO_KNOWN_HOSTS_FILE = "tmp/tmp_hostfile"
const val TEMP_DIR_TIMES_PATH = "tmp/times"

val USER_SCRIPT_NAME: String = File(USER_SCRIPT_PATH).name

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
            SshCommandBuilder.buildSshCommand(
                host = primaryInstancePublicIp,
                user = userOnInstance,
                identityFile = clusterKeyFilePath,
                knownHostsFile = pathToKnownHostsFile,
                extraOptions = listOf("-o", "ExitOnForwardFailure=yes", "-o", "StrictHostKeyChecking=no"),
                remoteCommand = "exit",
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
            SshCommandBuilder.buildSshCommand(
                host = primaryInstancePublicIp,
                user = userOnInstance,
                identityFile = clusterKeyFilePath,
                knownHostsFile = pathToKnownHostsFile,
                forcePseudoTerminal = true,
                extraOptions =
                    listOf(
                        "-o",
                        "ExitOnForwardFailure=yes",
                        "-o",
                        "LogLevel=ERROR",
                        "-o",
                        "StrictHostKeyChecking=no",
                    ),
                portForwarding = portForwardingElements,
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
        clusterKeyFilePath: String,
        allInstances: List<ClustEngineInstance>,
    ): Boolean {
        return transferAndExecuteScript(
            userOnInstance = userOnInstance,
            targetHost = publicIp,
            // Uses the default port 22
            targetPort = null,
            isPrimary = isPrimary,
            primaryInstancePrivateIp = primaryInstanceIP,
            clusterKeyFilePath = clusterKeyFilePath,
            allInstances = allInstances,
            knownHostsFilePath = PATH_TO_KNOWN_HOSTS_FILE,
        )
    }

    fun runScriptOnInstanceUsingOnePublicIp(
        primaryInstancePrivateIP: String,
        userOnInstance: String = "ubuntu",
        isPrimary: Boolean,
        clusterKeyFilePath: String,
        allInstances: List<ClustEngineInstance>,
        instanceIdx: Int,
    ): Boolean {
        val localHostFileManager = HostFileManager(instanceIdx.toString())
        localHostFileManager.deleteHostFile()
        val pathToKnownHostsFile = localHostFileManager.createHostFile()

        return transferAndExecuteScript(
            userOnInstance = userOnInstance,
            targetHost = "localhost",
            targetPort = instanceIdx,
            isPrimary = isPrimary,
            primaryInstancePrivateIp = primaryInstancePrivateIP,
            clusterKeyFilePath = clusterKeyFilePath,
            allInstances = allInstances,
            knownHostsFilePath = pathToKnownHostsFile,
        )
    }

    private fun transferAndExecuteScript(
        userOnInstance: String,
        targetHost: String,
        targetPort: Int?,
        isPrimary: Boolean,
        primaryInstancePrivateIp: String,
        clusterKeyFilePath: String,
        allInstances: List<ClustEngineInstance>,
        knownHostsFilePath: String,
    ): Boolean {
        if (!scriptExists(SCRIPT_PATH)) return false

        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()

        val baseCommandScp =
            SshCommandBuilder.buildScpCommand(
                sourcePath = SCRIPT_PATH,
                destinationPath = "$userOnInstance@$targetHost:",
                identityFile = clusterKeyFilePath,
                knownHostsFile = knownHostsFilePath,
                port = targetPort,
                extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
            )

        if (!executeSecureShellCommand(baseCommandScp, isScp = true)) return false

        if (scriptExists(USER_SCRIPT_PATH)) {
            val userScriptScp =
                SshCommandBuilder.buildScpCommand(
                    sourcePath = USER_SCRIPT_PATH,
                    destinationPath = "$userOnInstance@$targetHost:$USER_SCRIPT_NAME",
                    identityFile = clusterKeyFilePath,
                    knownHostsFile = knownHostsFilePath,
                    port = targetPort,
                    extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
                )

            if (!executeSecureShellCommand(userScriptScp, isScp = true)) {
                logger.warn("User payload script found locally but failed to transfer to $targetHost.")
                return false
            }
        }

        val clusterPrivateIps = allInstances.joinToString(" ") { it.privateIpAddress }
        val extraArgs =
            if (isPrimary) {
                arrayOf("-p", "-s", primaryInstancePrivateIp)
            } else {
                arrayOf("-s", primaryInstancePrivateIp)
            }

        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()

        val remoteScriptPath = "/home/$userOnInstance/$SCRIPT_NAME"
        val remoteExecution = "chmod +x $remoteScriptPath; $remoteScriptPath ${extraArgs.joinToString(" ")} $clusterPrivateIps"

        // Executes the script
        val sshCommand =
            SshCommandBuilder.buildSshCommand(
                host = targetHost,
                user = userOnInstance,
                identityFile = clusterKeyFilePath,
                knownHostsFile = knownHostsFilePath,
                port = targetPort,
                extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
                remoteCommand = remoteExecution,
            )

        return executeSecureShellCommand(sshCommand, isScp = false)
    }

    fun getTimesFromMachineUsingOnePublicIp(
        isPrimary: Boolean,
        clusterKeyFilePath: String,
        userOnInstance: String = "ubuntu",
        instanceIdxObj: AtomicInteger,
    ): Any {
        val port = instanceIdxObj.get()
        val tempFilePath = "$TEMP_DIR_TIMES_PATH/temp_data_localport_$port.txt"

        return try {
            preparationForScp(TEMP_DIR_TIMES_PATH)

            val pathToKnownHostsFile = "tmp/tmp_hostfile_${instanceIdxObj.get()}"

            val copyTimesCommand =
                SshCommandBuilder.buildScpCommand(
                    sourcePath = "$userOnInstance@localhost:/home/ubuntu/times.txt",
                    destinationPath = tempFilePath,
                    identityFile = clusterKeyFilePath,
                    knownHostsFile = pathToKnownHostsFile,
                    port = port,
                    extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
                )

            if (!executeScpAndLog(copyTimesCommand, port)) {
                return timesFromFileContent(isPrimary, null)
            }

            parseTimesFileAndCleanUp(isPrimary, tempFilePath, "port $port")
        } catch (e: Exception) {
            logger.error("Failed to get times from instance via port ${instanceIdxObj.get()}: ${e.message}")
            timesFromFileContent(isPrimary, null)
        }
    }

    fun getTimesFromMachine(
        isPrimary: Boolean,
        clusterKeyFilePath: String,
        userOnInstance: String = "ubuntu",
        publicIp: String,
        instanceIdxObj: AtomicInteger,
    ): Any {
        val tempFilePath = "$TEMP_DIR_TIMES_PATH/temp_data_$publicIp.txt"

        return try {
            preparationForScp(TEMP_DIR_TIMES_PATH)

            val pathToKnownHostsFile = "tmp/tmp_hostfile_${instanceIdxObj.get()}"

            val copyTimesCommand =
                SshCommandBuilder.buildScpCommand(
                    sourcePath = "$userOnInstance@$publicIp:/home/ubuntu/times.txt",
                    destinationPath = tempFilePath,
                    identityFile = clusterKeyFilePath,
                    knownHostsFile = pathToKnownHostsFile,
                    extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
                )

            // Executes the SCP command
            if (!executeScpAndLog(copyTimesCommand)) {
                return timesFromFileContent(isPrimary, null)
            }

            parseTimesFileAndCleanUp(isPrimary, tempFilePath, "IP $publicIp")
        } catch (e: Exception) {
            logger.error("Failed to get times from instance $publicIp: ${e.message}")
            timesFromFileContent(isPrimary, null)
        }
    }

    private fun preparationForScp(tempDirPath: String) {
        // Creates the temporary directory to store the copied file
        Files.createDirectories(Paths.get(tempDirPath))

        // Builds the SCP command to copy the times.txt file from the instance
        hostFileManager.deleteHostFile()
        hostFileManager.createHostFile()
    }

    private fun executeScpAndLog(
        command: Array<String>,
        port: Int? = null,
    ): Boolean {
        val success = executeSecureShellCommand(command, isScp = true)

        if (!success) {
            val target = if (port != null) "port $port" else "the instance"
            logger.warn("Could not retrieve times.txt from instance via $target.")
        }

        return success
    }

    private fun parseTimesFileAndCleanUp(
        isPrimary: Boolean,
        tempFilePath: String,
        targetIdentifier: String,
    ): Any {
        val downloadedFile = File(tempFilePath)

        if (!downloadedFile.exists() || downloadedFile.length() == 0L) {
            logger.warn("File times.txt was not downloaded correctly or is empty for $targetIdentifier.")
            return timesFromFileContent(isPrimary, null)
        }

        val lines = downloadedFile.readLines()

        downloadedFile.delete()

        if (isPrimary && lines.size < 5) {
            logger.warn("Primary times.txt on $targetIdentifier has less than 5 lines: $lines")
        } else if (!isPrimary && lines.size < 3) {
            logger.warn("Non-primary times.txt on $targetIdentifier has less than 3 lines: $lines")
        }

        return timesFromFileContent(isPrimary, lines)
    }
}

private fun scriptExists(scriptPath: String): Boolean {
    try {
        val scriptFile =
            File(scriptPath).takeIf { it.exists() }
                ?: File("../$scriptPath")

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
