package gui.i18n.languages.en

import gui.i18n.AppStrings

object EnStrings : AppStrings {
    // Common & Generic Words
    override val appTitle = "ClustEngine Panel"
    override val cancelBtn = "Cancel"
    override val confirmBtn = "Confirm"
    override val backBtn = "Back"
    override val startBtn = "Start"
    override val stopBtn = "Stop"
    override val deleteBtn = "Delete"
    override val providerLabel = "Provider:"
    override val instancesLabel = "Instances:"

    // Navigation & Sidebar
    override val savedClustersMenu = "Saved Clusters"
    override val dashboardMenu = "Cluster Dashboard"
    override val createClusterMenu = "Create Cluster"
    override val openMenuDesc = "Open Menu"
    override val collapseSidebarDesc = "Collapse Sidebar"

    // SavedClustersScreen
    override val homeTitle = "Saved Clusters"
    override val homeSubtitle = "Select a cluster to manage or create a new one."
    override val savedClustersTitle = "Saved Clusters"
    override val noClustersFoundTitle = "No clusters found"
    override val createFirstClusterDesc = "Create your first cluster to get started."
    override val createNewClusterBtn = "Create New Cluster"

    override fun clusterNodeCount(
        provider: String,
        size: Int,
    ): String {
        return "$provider • $size Nodes"
    }

    // SetupScreen (Create Cluster)
    override val setupTitle = "Cluster Configuration"
    override val clusterNameLabel = "Cluster Name"
    override val clusterNamePlaceholder = "e.g., production-cluster"
    override val instanceCountLabel = "Number of Instances"
    override val instanceCountPlaceholder = "e.g., 3"
    override val cloudProviderTitle = "Cloud Provider"
    override val initEngineBtn = "Initialize Engine"
    override val deployingBtn = "Deploying..."
    override val goToClustersBtn = "Go to Saved Clusters"
    override val instanceFlavorTitle = "Instance Type / Flavor"
    override val selectFlavorPlaceholder = "Select an instance type..."
    override val cancelClusterCreationBtn = "Cancel Creation"
    override val confirmClusterCancelCreationTitle = "Cancel Cluster Creation?"
    override val confirmCancelCreationDesc =
        "Are you sure you want to abort the creation process? This will attempt to clean up any created resources and cannot be undone."
    override val logCancelling = "Cancelling creation and cleaning up resources..."

    // ClusterDetails (Cluster View)
    override val dashboardTitle = "ClustEngine Dashboard"
    override val clusterManagement = "Cluster Management"
    override val clusterDetailsSection = "Cluster Details"
    override val nodesInformationSection = "Nodes Information"
    override val initClusterBtn = "Init Cluster"
    override val listInstancesBtn = "List Instances"
    override val deleteClusterBtn = "Delete Cluster"
    override val clusterStatusSection = "Cluster Status"
    override val primaryNodeLabel = "Primary Node"
    override val secondaryNodeLabel = "Secondary Node"
    override val clusterActionsSection = "Cluster Actions"
    override val viewDetailsContentDesc = "View Details"

    // Dashboard Dialogs
    override val confirmStopClusterTitle = "Stop Cluster?"
    override val confirmStopClusterDesc = "All nodes will be suspended. Are you sure you want to stop the entire cluster?"
    override val confirmDeleteClusterTitle = "Delete Cluster?"
    override val confirmDeleteClusterDesc = "This action is irreversible. The cluster and all its data will be destroyed. Continue?"

    // InstanceDetailsScreen (Single Node View)
    override val backToDashboard = "Back to Cluster Dashboard"
    override val nodeManagementTitle = "Node Management"
    override val instanceManagement = "Instance Management (Individual)"
    override val instanceDetailsSection = "Instance Details"
    override val nodeActionsSection = "Node Actions"
    override val instanceIdLabel = "Instance ID"
    override val instanceIdPlaceholder = "Enter numeric ID"
    override val publicIpLabel = "Public IP"
    override val privateIpLabel = "Private IP"
    override val publicDnsLabel = "Public DNS"
    override val startNodeBtn = "Start Node"
    override val stopNodeBtn = "Stop Node"
    override val terminateNodeBtn = "Terminate Node"
    override val terminateBtn = "Terminate"

    // Instance Dialogs
    override val confirmStopInstanceTitle = "Stop Instance?"
    override val confirmStopInstanceDesc = "The instance will be temporarily suspended. Confirm stop?"
    override val confirmDeleteInstanceTitle = "Delete Instance?"
    override val confirmDeleteInstanceDesc = "The selected instance will be permanently destroyed. Confirm action?"
    override val terminateInstanceDialogTitle = "Terminate Instance"

    override fun terminateInstanceDialogDesc(id: String) =
        "Are you sure you want to permanently delete instance $id? This action cannot be undone."

    // SettingsScreen
    override val settingsTitle = "Settings"
    override val themeLabel = "Application Theme"
    override val themeLight = "Light Mode"
    override val themeDark = "Dark Mode"
    override val themeAuto = "Auto (System)"
    override val languageLabel = "Language"
    override val versionLabel = "Application Version"
    override val docsLabel = "Documentation"
    override val tabGeneral = "General"
    override val selectFileDialogTitle = "Select File"
    override val filePath = "File Path"
    override val osImageId = "Image ID"
    override val osAvailabilityZone = "Availability Zone"
    override val osSecurityGroup = "Security Group"
    override val osNetworkId = "Network ID"

    // Script
    override val tabScriptInjection = "Script Injection"
    override val saveSettingsBtn = "Save Configurations"
    override val confirmSaveSettingsTitle = "Save Settings?"
    override val confirmSaveSettingsDesc =
        "Are you sure you want to save these configurations to your .env file? This will overwrite the existing configurations."
    override val baseScriptConfig = "Base Script Configuration"
    override val userScriptConfig = "User Script Configuration"
    override val browseBtn = "Browse"
    override val selectScriptDialogTitle = "Select Script File"
    override val dirPath = "Directory Path"
    override val fileName = "File Name"

    // Credentials
    override val tabCredentials = "Cloud Credentials"
    override val scriptConfigSection = "Script Configuration Paths"
    override val awsConfigSection = "AWS Credentials & Config"
    override val openStackConfigSection = "OpenStack Credentials & Config"
    override val awsRegion = "Region"
    override val keyFileName = "Key File Name"
    override val keyFilePath = "Key File Path"
    override val awsSubnetId = "Subnet ID"
    override val awsSecGroupId = "Security Group ID"
    override val awsImageId = "Image ID"
    override val osUserName = "User Name"
    override val osDomain = "Domain"
    override val osPassword = "Password"
    override val osProjectName = "Project Name"
    override val osBaseUrl = "Base URL"
    override val awsKeyFile = "AWS Key File (.pem)"
    override val osKeyFile = "OpenStack Key File (.pem)"

    // Terminal / Console
    override val terminalTitle = ">_ TERMINAL LOGS"
    override val terminalEmpty = "Waiting for actions..."
    override val consoleOutput = "Console Output"

    // Terminal Logs
    override fun logWelcome() = "Welcome to ClustEngine GUI."

    override fun logErrorProvider() = "Error: Select a provider."

    override fun logInitEngine(provider: String) = "Initializing $provider engine..."

    override fun logEngineReady() = "Engine initialized. Ready to receive commands."

    override fun logInitCluster() = "Executing Initialization..."

    override fun logListInstances() = "Listing Instances..."

    override fun logStartCluster() = "Starting Cluster..."

    override fun logStopCluster() = "Stopping Cluster..."

    override fun logDeleteCluster() = "Deleting Cluster..."

    override fun logStartInstance(id: Int) = "Starting instance ID: $id..."

    override fun logStopInstance(id: Int) = "Stopping instance ID: $id..."

    override fun logDeleteInstance(id: Int) = "Deleting instance ID: $id..."

    override fun logInvalidId() = "Error: Invalid ID."

    override fun logGenericError(error: String) = "ERROR: $error"
}
