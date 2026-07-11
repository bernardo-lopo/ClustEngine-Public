package core.persistance

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.domain.LiveCluster
import org.slf4j.LoggerFactory
import java.io.File

object ClusterPersistenceManager {
    private val logger = LoggerFactory.getLogger("ClusterPersistenceManager")
    private val mapper =
        jacksonObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }

    private val saveDirectory = File("saved_clusters")

    init {
        if (!saveDirectory.exists()) {
            saveDirectory.mkdirs()
        }
    }

    fun clusterExists(clusterName: String): Boolean {
        return File(saveDirectory, "$clusterName.json").exists()
    }

    fun saveCluster(config: LiveCluster) {
        val file = File(saveDirectory, "${config.clusterName}.json")

        if (file.exists()) {
            throw IllegalStateException("A cluster with the name '${config.clusterName}' already exists.")
        }

        try {
            val jsonString = mapper.writeValueAsString(config)
            file.writeText(jsonString)
            logger.info("Successfully saved cluster configuration to ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to write cluster file: ${e.message}")
            throw e
        }
    }

    fun updateCluster(config: LiveCluster) {
        val file = File(saveDirectory, "${config.clusterName}.json")

        try {
            val jsonString = mapper.writeValueAsString(config)
            file.writeText(jsonString)
            logger.info("Successfully updated cluster configuration at ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to update cluster file: ${e.message}")
            throw e
        }
    }

    fun deleteCluster(clusterName: String): Boolean {
        val file = File(saveDirectory, "$clusterName.json")

        return if (file.exists()) {
            val isDeleted = file.delete()
            if (isDeleted) {
                logger.info("Successfully deleted local cluster configuration: ${file.name}")
            } else {
                logger.error("Failed to delete local cluster configuration: ${file.name}")
            }
            isDeleted
        } else {
            logger.warn("No local configuration file found for cluster name: $clusterName")
            false
        }
    }

    fun loadAllClusters(): List<LiveCluster> {
        val configs = mutableListOf<LiveCluster>()
        val files = saveDirectory.listFiles { file -> file.extension == "json" } ?: return emptyList()

        for (file in files) {
            try {
                val jsonString = file.readText()
                val config: LiveCluster = mapper.readValue(jsonString)
                configs.add(config)
            } catch (e: Exception) {
                logger.error("Failed to read cluster configuration from ${file.name}: ${e.message}", e)
            }
        }
        return configs
    }
}
