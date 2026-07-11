package gui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.ClusterEngineFactoryInterface
import core.ClusterManager
import core.domain.ClustEngineInstance
import core.domain.LiveCluster
import core.domain.MultiIpRouting
import core.domain.SingleIpRouting
import core.persistance.ClusterPersistenceManager
import gui.i18n.AppStrings
import gui.state.enums.AppScreen
import gui.state.enums.CloudProvider
import gui.state.enums.UIRoutingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class ClusterOperationsManager(
    private val coroutineScope: CoroutineScope,
    private val factory: ClusterEngineFactoryInterface,
    private val console: ConsoleManager,
    private val form: FormManager,
    private val nav: NavigationManager,
    private val getStrings: () -> AppStrings,
) {
    var savedClusters by mutableStateOf<List<LiveCluster>>(emptyList())
    var activeCluster by mutableStateOf<LiveCluster?>(null)
        private set
    private var activeEngine by mutableStateOf<ClusterManager?>(null)
    var activeInstance by mutableStateOf<ClustEngineInstance?>(null)
        private set

    var isClusterBeingCreated by mutableStateOf(false)
        private set
    var isCreationSuccess by mutableStateOf(false)
        private set
    var creationTimeInSeconds by mutableStateOf(0L)
        private set

    private var clusterCreationJob: Job? = null
    private val strings get() = getStrings()

    fun resetCreationState() {
        isCreationSuccess = false
        creationTimeInSeconds = 0L
    }

    fun loadSavedClusters() {
        savedClusters = ClusterPersistenceManager.loadAllClusters()
    }

    fun selectAndNavigateToDashboard(cluster: LiveCluster) {
        activeCluster = cluster
        form.clusterName = cluster.clusterName
        form.clusterSize = cluster.clusterSize.toString()
        form.selectedProvider =
            if (cluster.provider.equals("AWSClusterEngine", ignoreCase = true)) {
                CloudProvider.AWS
            } else {
                CloudProvider.OpenStack
            }

        try {
            val service = factory.createFromSavedCluster(cluster)
            activeEngine = ClusterManager(service)
            console.log("Dashboard loaded for ${cluster.clusterName}")
        } catch (e: Exception) {
            console.log("Error initializing engine for ${cluster.clusterName}: ${e.message}")
            activeEngine = null
        }
        nav.navigateTo(AppScreen.Dashboard)
    }

    fun selectAndNavigateToInstance(instance: ClustEngineInstance) {
        activeInstance = instance
        nav.navigateTo(AppScreen.InstanceDetails)
    }

    private fun executeCloudAction(
        successLog: String? = null,
        action: suspend () -> Unit,
    ) {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) { action() }
                if (successLog != null) console.log(successLog)
            } catch (e: Exception) {
                console.log(strings.logGenericError(e.message ?: "Unknown error"))
                e.printStackTrace()
            }
        }
    }

    fun startInstance(id: String) = executeCloudAction(strings.logStartCluster()) { activeEngine?.startById(id) }

    fun stopInstance(id: String) = executeCloudAction(strings.logStopCluster()) { activeEngine?.stopById(id) }

    fun startCluster() = executeCloudAction(strings.logStartCluster()) { activeEngine?.start() }

    fun stopCluster() = executeCloudAction(strings.logStopCluster()) { activeEngine?.stop() }

    fun deleteInstance(id: String) =
        executeCloudAction(strings.logDeleteCluster()) {
            val currentCluster = activeCluster ?: return@executeCloudAction
            activeEngine?.deleteInstance(id)
            val updatedCluster =
                currentCluster.copy(
                    instances = currentCluster.instances.filter { it.id != id },
                    clusterSize = currentCluster.clusterSize - 1,
                )
            ClusterPersistenceManager.updateCluster(updatedCluster)
            activeCluster = updatedCluster
            savedClusters = savedClusters.map { if (it.clusterId == updatedCluster.clusterId) updatedCluster else it }
            nav.navigateTo(AppScreen.Dashboard)
        }

    fun deleteCluster() =
        executeCloudAction(strings.logDeleteCluster()) {
            val clusterToDelete = activeCluster ?: return@executeCloudAction
            activeEngine?.deleteCluster()
            ClusterPersistenceManager.deleteCluster(clusterToDelete.clusterName)
            savedClusters = savedClusters.filter { it.clusterId != clusterToDelete.clusterId }
            activeCluster = null
            activeEngine = null
            nav.navigateTo(AppScreen.SavedClusters)
        }

    fun initEngine() {
        val provider = form.selectedProvider ?: return
        val name = form.clusterName.takeIf { it.isNotBlank() } ?: "DefaultCluster"
        val size = form.clusterSize.toIntOrNull() ?: 2
        val flavorId = form.selectedFlavor?.id ?: return
        val ipRoutingMode =
            when (form.selectedRoutingMode) {
                UIRoutingMode.SINGLE_IP -> SingleIpRouting()
                UIRoutingMode.MULTI_IP -> MultiIpRouting()
                null -> return
            }

        if (isClusterBeingCreated) return

        clusterCreationJob =
            coroutineScope.launch {
                var timerJob: Job? = null
                try {
                    console.log(strings.logInitEngine(provider.name))
                    isClusterBeingCreated = true
                    isCreationSuccess = false
                    creationTimeInSeconds = 0L

                    timerJob =
                        launch {
                            while (true) {
                                delay(1000.milliseconds)
                                creationTimeInSeconds++
                            }
                        }

                    tempFilesCleanUp()

                    val service =
                        when (provider) {
                            CloudProvider.AWS -> factory.createAWSService(name, size, flavorId, ipRoutingMode)
                            CloudProvider.OpenStack -> factory.createOpenStackService(name, size, flavorId, ipRoutingMode)
                        }

                    activeEngine = ClusterManager(service)
                    withContext(Dispatchers.IO) { activeEngine?.initCluster() }

                    console.log("Cluster created successfully via ${provider.name}.")
                    loadSavedClusters()
                    isCreationSuccess = true
                } catch (e: Exception) {
                    console.log("Error initializing engine: ${e.message}")
                    e.printStackTrace()
                } finally {
                    isClusterBeingCreated = false
                    timerJob?.cancel()
                }
            }
    }

    fun cancelClusterCreation() =
        executeCloudAction(null) {
            if (!isClusterBeingCreated) return@executeCloudAction
            console.log(strings.logCancelling)

            clusterCreationJob?.cancel()
            activeEngine?.deleteCluster()
            tempFilesCleanUp()

            console.log("Cluster creation successfully cancelled.")
            isClusterBeingCreated = false
            activeEngine = null
            creationTimeInSeconds = 0L
            isCreationSuccess = false
        }

    private suspend fun tempFilesCleanUp() =
        withContext(Dispatchers.IO) {
            File("tmp").deleteRecursively()
        }
}
