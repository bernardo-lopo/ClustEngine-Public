package tester

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import core.ClusterManager
import core.ClusterServiceProviderInterface
import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary
import core.util.ConfigurationsLoader
import core.util.TimeMeasure.Companion.timeElapsed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tester.dto.PartialTimesOutput
import tester.dto.TestIteration
import tester.dto.TestedCluster
import java.io.File
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val logger: Logger = LoggerFactory.getLogger("Tester")

class Tester(
    instance: ClusterServiceProviderInterface,
    val nInstances: Int,
    val config: ConfigurationsLoader,
    val timeToWait: Int = 2,
) : ClusterManager(instance) {
    private var isDeleted = true
    private val jsonWriter = jacksonObjectMapper()

    private fun checkAndDelete() {
        if (!isDeleted) {
            deleteCluster()
            isDeleted = true
        }
    }

    fun run(): Boolean {
        val numberOfIterations = config.getProperty("test.iterations").toInt()

        val fileName = "Cluster_${nInstances}_rep_$numberOfIterations.json"
        val file = File(fileName)
        if (file.exists()) {
            logger.error("The file $fileName already exists. Please delete it before starting.")
            return false
        }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("Shutdown initiated, closing server...")
                safeExit()
            },
        )

        val data = mutableListOf<TestedCluster>()

        repeat(numberOfIterations) { cluster ->
            var shouldCleanup = false
            try {
                checkAndDelete()

                isDeleted = false

                logger.info("Iteration: ${cluster + 1}")

                var clusterData: Pair<PartialTimesPrimary?, List<PartialTimesNonPrimary?>>? = null
                val time =
                    timeElapsed {
                        clusterData = initCluster()
                    }

                logger.info("Data stored")
                data.add(
                    TestedCluster(
                        iteration = TestIteration(cluster + 1, numberOfIterations).toString(),
                        totalTime = time,
                        partialTimes =
                            PartialTimesOutput(
                                pMachine = clusterData!!.first,
                                nPMachines = clusterData!!.second,
                            ),
                        numberOfInstances = nInstances,
                        testedAt =
                            LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy HH:mm:ss",
                                    Locale.getDefault(Locale.Category.FORMAT),
                                ),
                            ),
                    ),
                )

                shouldCleanup = true
            } catch (e: UnknownHostException) {
                logger.error("Iteration ${cluster + 1}: Unknown host - ${e.message}")
                logger.warn("Waiting $timeToWait minutes before continuing to next iteration...")
                shouldCleanup = true
                Thread.sleep(timeToWait.toLong() * 60 * 1000)
            } catch (e: Exception) {
                logger.error("Iteration ${cluster + 1}: Unexpected error - ${e.message}", e)
                shouldCleanup = true
            } finally {
                if (shouldCleanup) {
                    checkAndDelete()
                }
            }
        }

        try {
            val testResults = jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(data)
            file.writeText(testResults)
            logger.info("Results successfully saved to $fileName")
            return true
        } catch (e: Exception) {
            logger.error("Failed to save results: ${e.message}", e)
        }
        return false
    }

    private fun safeExit() {
        try {
            if (isInited() == true || !isDeleted) {
                logger.info("Cleaning up cluster before shutdown...")
                deleteCluster()
                isDeleted = true
            }
        } catch (e: Exception) {
            logger.error("Error while shutting down", e)
        } finally {
            logger.info("Shutdown hook finished")
        }
    }
}
