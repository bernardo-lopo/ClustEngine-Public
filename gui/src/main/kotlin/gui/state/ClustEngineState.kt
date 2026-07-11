package gui.state

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.ClusterEngineFactoryInterface
import core.domain.ClustEngineInstance
import core.domain.ClustEngineInstanceType
import core.domain.LiveCluster
import core.util.GeneralSettingsManager
import gui.i18n.AppStrings
import gui.i18n.Language
import gui.i18n.languages.en.EnStrings
import gui.i18n.languages.pt.PtStrings
import gui.state.enums.AppScreen
import gui.state.enums.CloudProvider
import gui.state.enums.ThemeMode
import gui.state.enums.UIRoutingMode
import kotlinx.coroutines.CoroutineScope

class ClustEngineState(
    coroutineScope: CoroutineScope,
    clusterEngineFactory: ClusterEngineFactoryInterface,
) {
    private val consoleManager = ConsoleManager(coroutineScope)
    private val navManager = NavigationManager()
    private val formManager = FormManager()
    private val opsManager =
        ClusterOperationsManager(
            coroutineScope,
            clusterEngineFactory,
            consoleManager,
            formManager,
            navManager,
            { strings },
        )
    val appVersion = "0.1.0-beta"
    var themeMode by mutableStateOf(ThemeMode.Auto)
    var currentLanguage by mutableStateOf(Language.Auto)

    private val _strings =
        derivedStateOf {
            val targetLang = if (currentLanguage == Language.Auto) SystemPreferences.getSystemLanguage() else currentLanguage
            if (targetLang == Language.PT) PtStrings else EnStrings
        }
    val strings: AppStrings get() = _strings.value

    val isDarkTheme: Boolean
        get() =
            when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.Auto -> {
                    val os = System.getProperty("os.name").lowercase()
                    if (os.contains("linux")) SystemPreferences.isLinuxDarkTheme() else false
                }
            }

    init {
        System.setProperty("kotlinx.coroutines.io.parallelism", "512")
        GeneralSettingsManager.loadTheme()?.let { themeMode = ThemeMode.valueOf(it) }
        GeneralSettingsManager.loadLanguage()?.let { currentLanguage = Language.valueOf(it) }

        opsManager.loadSavedClusters()
        if (opsManager.savedClusters.isEmpty()) navManager.navigateTo(AppScreen.Setup)
        consoleManager.log(strings.logWelcome())
    }

    val consoleLogs get() = consoleManager.logs
    val currentScreen get() = navManager.currentScreen
    val isSidebarVisible get() = navManager.isSidebarVisible
    val activeCluster get() = opsManager.activeCluster
    val activeInstance get() = opsManager.activeInstance
    val isClusterBeingCreated get() = opsManager.isClusterBeingCreated
    val isCreationSuccess get() = opsManager.isCreationSuccess
    val creationTimeInSeconds get() = opsManager.creationTimeInSeconds

    var savedClusters: List<LiveCluster>
        get() = opsManager.savedClusters
        set(value) {
            opsManager.savedClusters = value
        }

    var clusterName: String
        get() = formManager.clusterName
        set(value) {
            formManager.clusterName = value
        }

    var clusterSize: String
        get() = formManager.clusterSize
        set(value) {
            formManager.clusterSize = value
        }

    var selectedProvider: CloudProvider?
        get() = formManager.selectedProvider
        set(value) {
            formManager.selectedProvider = value
        }

    var availableFlavors: List<ClustEngineInstanceType>
        get() = formManager.availableFlavors
        set(value) {
            formManager.availableFlavors = value
        }

    var selectedFlavor: ClustEngineInstanceType?
        get() = formManager.selectedFlavor
        set(value) {
            formManager.selectedFlavor = value
        }

    var selectedRoutingMode: UIRoutingMode?
        get() = formManager.selectedRoutingMode
        set(value) {
            formManager.selectedRoutingMode = value
        }

    fun navigateTo(screen: AppScreen) = navManager.navigateTo(screen)

    fun toggleSidebar() = navManager.toggleSidebar()

    fun navigateToSetup() {
        if (opsManager.isClusterBeingCreated) {
            navManager.navigateTo(AppScreen.Setup)
            return
        }
        formManager.clear()
        opsManager.resetCreationState()
        consoleManager.clear()
        navManager.navigateTo(AppScreen.Setup)
    }

    fun openDocumentation() = SystemPreferences.openDocumentation { consoleManager.log(strings.logGenericError(it)) }

    fun selectAndNavigateToDashboard(cluster: LiveCluster) = opsManager.selectAndNavigateToDashboard(cluster)

    fun selectAndNavigateToInstance(instance: ClustEngineInstance) = opsManager.selectAndNavigateToInstance(instance)

    fun startInstance(id: String) = opsManager.startInstance(id)

    fun stopInstance(id: String) = opsManager.stopInstance(id)

    fun deleteInstance(id: String) = opsManager.deleteInstance(id)

    fun startCluster() = opsManager.startCluster()

    fun stopCluster() = opsManager.stopCluster()

    fun deleteCluster() = opsManager.deleteCluster()

    fun initEngine() = opsManager.initEngine()

    fun cancelClusterCreation() = opsManager.cancelClusterCreation()
}
