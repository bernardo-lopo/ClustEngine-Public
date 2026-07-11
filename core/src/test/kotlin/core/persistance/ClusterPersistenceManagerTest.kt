package core.persistance

import core.domain.ClustEngineInstance
import core.domain.LiveCluster
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class ClusterPersistenceManagerTest {
    private val saveDirectory = File("saved_clusters")

    @BeforeEach
    fun setUp() {
        // Ensure a clean slate before each test
        if (saveDirectory.exists()) {
            saveDirectory.deleteRecursively()
        }
        saveDirectory.mkdirs()
    }

    @AfterEach
    fun tearDown() {
        // Clean up after the test finishes
        if (saveDirectory.exists()) {
            saveDirectory.deleteRecursively()
        }
    }

    // Helper function to generate mock data
    private fun createDummyCluster(name: String): LiveCluster {
        val dummyInstance =
            ClustEngineInstance(
                id = UUID.randomUUID().toString(),
                privateIpAddress = "10.0.0.1",
                publicIp = "192.168.1.1",
                publicDns = "ec2-test.compute.amazonaws.com",
            )

        return LiveCluster(
            clusterId = UUID.randomUUID().toString(),
            clusterName = name,
            provider = "AWSClusterEngine",
            primaryInstance = dummyInstance,
            instances = listOf(dummyInstance),
            clusterKeyFilePath = "/path/to/key.pem",
            clusterSize = 1,
            instanceTypeId = "t2.micro",
        )
    }

    @Test
    fun `saveCluster should create a json file with the cluster name`() {
        val clusterName = "AlphaCluster"
        val cluster = createDummyCluster(clusterName)

        ClusterPersistenceManager.saveCluster(cluster)

        val expectedFile = File(saveDirectory, "$clusterName.json")
        assertTrue(expectedFile.exists(), "The JSON file should be created on the disk.")
        assertTrue(ClusterPersistenceManager.clusterExists(clusterName), "clusterExists should return true.")
    }

    @Test
    fun `saveCluster should throw IllegalStateException if cluster name already exists`() {
        val clusterName = "BetaCluster"
        val cluster1 = createDummyCluster(clusterName)
        val cluster2 = createDummyCluster(clusterName)

        // First save should succeed
        ClusterPersistenceManager.saveCluster(cluster1)

        // Second save should fail
        val exception =
            assertThrows(IllegalStateException::class.java) {
                ClusterPersistenceManager.saveCluster(cluster2)
            }

        assertTrue(exception.message!!.contains(clusterName))
    }

    @Test
    fun `loadAllClusters should return all saved json configurations`() {
        val cluster1 = createDummyCluster("ClusterOne")
        val cluster2 = createDummyCluster("ClusterTwo")

        ClusterPersistenceManager.saveCluster(cluster1)
        ClusterPersistenceManager.saveCluster(cluster2)

        val loadedClusters = ClusterPersistenceManager.loadAllClusters()

        assertEquals(2, loadedClusters.size, "Should load exactly two clusters.")
        val names = loadedClusters.map { it.clusterName }
        assertTrue(names.contains("ClusterOne"))
        assertTrue(names.contains("ClusterTwo"))
    }

    @Test
    fun `loadAllClusters should ignore non-json files`() {
        val cluster = createDummyCluster("ValidCluster")
        ClusterPersistenceManager.saveCluster(cluster)

        // Create a dummy txt file in the directory
        File(saveDirectory, "ignore_me.txt").writeText("This is not a cluster")

        val loadedClusters = ClusterPersistenceManager.loadAllClusters()

        assertEquals(1, loadedClusters.size, "Should only load the .json file and ignore the .txt file.")
        assertEquals("ValidCluster", loadedClusters.first().clusterName)
    }

    @Test
    fun `deleteCluster should remove the file and return true when file exists`() {
        val clusterName = "GammaCluster"
        val cluster = createDummyCluster(clusterName)

        ClusterPersistenceManager.saveCluster(cluster)
        assertTrue(ClusterPersistenceManager.clusterExists(clusterName))

        val result = ClusterPersistenceManager.deleteCluster(clusterName)

        assertTrue(result, "deleteCluster should return true when successful.")
        assertFalse(ClusterPersistenceManager.clusterExists(clusterName), "The file should no longer exist.")
    }

    @Test
    fun `deleteCluster should return false when file does not exist`() {
        val result = ClusterPersistenceManager.deleteCluster("NonExistentCluster")

        assertFalse(result, "deleteCluster should return false if the cluster was not found.")
    }
}
