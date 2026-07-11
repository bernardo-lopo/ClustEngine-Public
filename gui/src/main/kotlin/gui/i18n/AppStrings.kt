package gui.i18n

import androidx.compose.runtime.compositionLocalOf

enum class Language { Auto, PT, EN }

val LocalStrings =
    compositionLocalOf<AppStrings> {
        error("No translations provided")
    }

interface AppStrings {
    // Common & Generic Words
    val appTitle: String
    val cancelBtn: String
    val confirmBtn: String
    val backBtn: String
    val startBtn: String
    val stopBtn: String
    val deleteBtn: String
    val providerLabel: String
    val instancesLabel: String

    // Navigation & Sidebar
    val savedClustersMenu: String
    val dashboardMenu: String
    val createClusterMenu: String
    val openMenuDesc: String
    val collapseSidebarDesc: String

    // SavedClustersScreen
    val homeTitle: String
    val homeSubtitle: String
    val savedClustersTitle: String
    val noClustersFoundTitle: String
    val createFirstClusterDesc: String
    val createNewClusterBtn: String

    fun clusterNodeCount(
        provider: String,
        size: Int,
    ): String

    // SetupScreen (Create Cluster)
    val setupTitle: String
    val clusterNameLabel: String
    val clusterNamePlaceholder: String
    val instanceCountLabel: String
    val instanceCountPlaceholder: String
    val cloudProviderTitle: String
    val initEngineBtn: String
    val deployingBtn: String
    val goToClustersBtn: String
    val instanceFlavorTitle: String
    val selectFlavorPlaceholder: String
    val cancelClusterCreationBtn: String
    val confirmClusterCancelCreationTitle: String
    val confirmCancelCreationDesc: String
    val logCancelling: String

    // Cluster Details (Cluster View)
    val dashboardTitle: String
    val clusterManagement: String
    val clusterDetailsSection: String
    val nodesInformationSection: String
    val initClusterBtn: String
    val listInstancesBtn: String
    val deleteClusterBtn: String
    val clusterStatusSection: String
    val primaryNodeLabel: String
    val secondaryNodeLabel: String
    val clusterActionsSection: String
    val viewDetailsContentDesc: String

    // Dashboard Dialogs
    val confirmStopClusterTitle: String
    val confirmStopClusterDesc: String
    val confirmDeleteClusterTitle: String
    val confirmDeleteClusterDesc: String

    // InstanceDetailsScreen (Single Node View)
    val backToDashboard: String
    val nodeManagementTitle: String
    val instanceManagement: String
    val instanceDetailsSection: String
    val nodeActionsSection: String
    val instanceIdLabel: String
    val instanceIdPlaceholder: String
    val publicIpLabel: String
    val privateIpLabel: String
    val publicDnsLabel: String
    val startNodeBtn: String
    val stopNodeBtn: String
    val terminateNodeBtn: String
    val terminateBtn: String

    // Instance Dialogs
    val confirmStopInstanceTitle: String
    val confirmStopInstanceDesc: String
    val confirmDeleteInstanceTitle: String
    val confirmDeleteInstanceDesc: String
    val terminateInstanceDialogTitle: String

    fun terminateInstanceDialogDesc(id: String): String

    // SettingsScreen
    val settingsTitle: String
    val themeLabel: String
    val themeLight: String
    val themeDark: String
    val themeAuto: String
    val languageLabel: String
    val versionLabel: String
    val docsLabel: String
    val saveSettingsBtn: String
    val confirmSaveSettingsTitle: String
    val confirmSaveSettingsDesc: String
    val browseBtn: String
    val selectScriptDialogTitle: String
    val dirPath: String
    val selectFileDialogTitle: String
    val filePath: String
    val osKeyFile: String
    val osImageId: String
    val osAvailabilityZone: String
    val osSecurityGroup: String
    val osNetworkId: String

    // Script
    val tabScriptInjection: String
    val scriptConfigSection: String
    val tabGeneral: String
    val baseScriptConfig: String
    val userScriptConfig: String
    val fileName: String

    // Credentials
    val tabCredentials: String
    val awsConfigSection: String
    val openStackConfigSection: String
    val awsRegion: String
    val keyFileName: String
    val keyFilePath: String
    val awsSubnetId: String
    val awsSecGroupId: String
    val awsImageId: String
    val osUserName: String
    val osDomain: String
    val osPassword: String
    val osProjectName: String
    val osBaseUrl: String
    val awsKeyFile: String

    // Terminal / Console
    val terminalTitle: String
    val terminalEmpty: String
    val consoleOutput: String

    // Terminal Logs
    fun logWelcome(): String

    fun logErrorProvider(): String

    fun logInitEngine(provider: String): String

    fun logEngineReady(): String

    fun logInitCluster(): String

    fun logListInstances(): String

    fun logStartCluster(): String

    fun logStopCluster(): String

    fun logDeleteCluster(): String

    fun logStartInstance(id: Int): String

    fun logStopInstance(id: Int): String

    fun logDeleteInstance(id: Int): String

    fun logInvalidId(): String

    fun logGenericError(error: String): String
}
