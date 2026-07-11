package gui.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import core.util.GeneralSettingsManager
import core.util.ScriptConfigLoader
import gui.i18n.AppStrings
import gui.i18n.Language
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.state.enums.ThemeMode
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockState: ClustEngineState
    private lateinit var fakeStrings: AppStrings

    @Before
    fun setup() {
        mockkObject(ScriptConfigLoader)
        every { ScriptConfigLoader.getEnvOrNull(any()) } returns ""
        every { ScriptConfigLoader.saveEnvVariables(any()) } just Runs

        mockkObject(GeneralSettingsManager)
        every { GeneralSettingsManager.saveSettings(any(), any()) } just Runs

        mockState = mockk(relaxed = true)
        every { mockState.themeMode } returns ThemeMode.Auto
        every { mockState.currentLanguage } returns Language.Auto
        every { mockState.appVersion } returns "0.1.0-test"
        every { mockState.openDocumentation() } just Runs

        fakeStrings =
            object : AppStrings {
                override val settingsTitle = "Settings"
                override val tabGeneral = "General Tab"
                override val tabScriptInjection = "Scripts Tab"
                override val tabCredentials = "Creds Tab"
                override val themeLabel = "Theme"
                override val themeLight = "Light"
                override val themeDark = "Dark"
                override val themeAuto = "Auto"
                override val languageLabel = "Language"
                override val versionLabel = "Version"
                override val docsLabel = "Docs"
                override val scriptConfigSection = "Script Section"
                override val baseScriptConfig = "Base Script"
                override val userScriptConfig = "User Script"
                override val fileName = "File Name"
                override val browseBtn = "Browse"
                override val selectScriptDialogTitle = "Select Script"
                override val dirPath = "Dir Path"
                override val selectFileDialogTitle = "Select File"
                override val filePath = "Path"
                override val awsConfigSection = "AWS Config"
                override val openStackConfigSection = "OS Config"
                override val awsRegion = "Region"
                override val keyFileName = "Key File Name"
                override val keyFilePath = "Key File Path"
                override val awsKeyFile = "AWS Key"
                override val osKeyFile = "OS Key"
                override val osImageId = ""
                override val osAvailabilityZone = ""
                override val osSecurityGroup = ""
                override val osNetworkId = ""
                override val terminalTitle = ""
                override val terminalEmpty = ""
                override val consoleOutput = ""

                override fun logWelcome() = ""

                override fun logErrorProvider() = ""

                override fun logInitEngine(provider: String) = ""

                override fun logEngineReady() = ""

                override fun logInitCluster() = ""

                override fun logListInstances() = ""

                override fun logStartCluster() = ""

                override fun logStopCluster() = ""

                override fun logDeleteCluster() = ""

                override fun logStartInstance(id: Int) = ""

                override fun logStopInstance(id: Int) = ""

                override fun logDeleteInstance(id: Int) = ""

                override fun logInvalidId() = ""

                override fun logGenericError(error: String) = ""

                override val awsSubnetId = "Subnet"
                override val awsSecGroupId = "SecGroup"
                override val awsImageId = "Image"
                override val osUserName = "User"
                override val osDomain = "Domain"
                override val osPassword = "Password"
                override val osProjectName = "Project"
                override val osBaseUrl = "URL"
                override val saveSettingsBtn = "Save All"
                override val confirmSaveSettingsTitle = "Confirm?"
                override val confirmSaveSettingsDesc = "Are you sure?"
                override val confirmBtn = "Yes"
                override val backBtn = ""
                override val startBtn = ""
                override val stopBtn = ""
                override val deleteBtn = ""
                override val providerLabel = ""
                override val instancesLabel = ""
                override val savedClustersMenu = ""
                override val dashboardMenu = ""
                override val createClusterMenu = ""
                override val openMenuDesc = ""
                override val collapseSidebarDesc = ""
                override val homeTitle = ""
                override val homeSubtitle = ""
                override val savedClustersTitle = ""
                override val noClustersFoundTitle = ""
                override val createFirstClusterDesc = ""
                override val createNewClusterBtn = ""

                override fun clusterNodeCount(
                    provider: String,
                    size: Int,
                ) = ""

                override val setupTitle = ""
                override val clusterNameLabel = ""
                override val clusterNamePlaceholder = ""
                override val instanceCountLabel = ""
                override val instanceCountPlaceholder = ""
                override val cloudProviderTitle = ""
                override val initEngineBtn = ""
                override val deployingBtn = ""
                override val goToClustersBtn = ""
                override val instanceFlavorTitle: String = ""
                override val selectFlavorPlaceholder: String = ""
                override val cancelClusterCreationBtn: String = ""
                override val confirmClusterCancelCreationTitle: String = ""
                override val confirmCancelCreationDesc: String = ""
                override val logCancelling: String = ""
                override val dashboardTitle = ""
                override val clusterManagement = ""
                override val clusterDetailsSection = ""
                override val nodesInformationSection = ""
                override val initClusterBtn = ""
                override val listInstancesBtn = ""
                override val deleteClusterBtn = ""
                override val clusterStatusSection: String = ""
                override val primaryNodeLabel: String = ""
                override val secondaryNodeLabel: String = ""
                override val clusterActionsSection: String = ""
                override val viewDetailsContentDesc: String = ""
                override val confirmStopClusterTitle = ""
                override val confirmStopClusterDesc = ""
                override val confirmDeleteClusterTitle = ""
                override val confirmDeleteClusterDesc = ""
                override val backToDashboard = ""
                override val nodeManagementTitle = ""
                override val instanceManagement = ""
                override val instanceDetailsSection = ""
                override val nodeActionsSection = ""
                override val instanceIdLabel = ""
                override val instanceIdPlaceholder = ""
                override val publicIpLabel = ""
                override val privateIpLabel = ""
                override val publicDnsLabel = ""
                override val startNodeBtn = ""
                override val stopNodeBtn = ""
                override val terminateNodeBtn = ""
                override val terminateBtn = ""
                override val confirmStopInstanceTitle = ""
                override val confirmStopInstanceDesc = ""
                override val confirmDeleteInstanceTitle = ""
                override val confirmDeleteInstanceDesc = ""
                override val terminateInstanceDialogTitle = ""

                override fun terminateInstanceDialogDesc(id: String) = ""

                override val appTitle = ""
                override val cancelBtn = "No"
            }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun setContent() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides fakeStrings) {
                SettingsScreen(state = mockState)
            }
        }
    }

    @Test
    fun `General Tab renders correctly and Save button is visible`() {
        setContent()

        composeTestRule.onAllNodesWithText(fakeStrings.settingsTitle)[0].assertExists()

        composeTestRule.onNodeWithText(fakeStrings.themeLabel, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText(fakeStrings.languageLabel, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText(fakeStrings.versionLabel, ignoreCase = true).assertExists()

        composeTestRule.onNodeWithText(fakeStrings.themeLight).assertExists()
        composeTestRule.onNodeWithText(fakeStrings.themeDark).assertExists()
        composeTestRule.onAllNodesWithText(fakeStrings.themeAuto).assertCountEquals(2)

        composeTestRule.onNodeWithText(fakeStrings.saveSettingsBtn).assertExists()
    }

    @Test
    fun `Script Injection Tab renders fields and Save button becomes visible`() {
        setContent()

        composeTestRule.onNodeWithText(fakeStrings.tabScriptInjection).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(fakeStrings.scriptConfigSection, ignoreCase = true).assertExists()

        composeTestRule.onNodeWithText(fakeStrings.baseScriptConfig).assertExists()
        composeTestRule.onNodeWithText(fakeStrings.userScriptConfig).assertExists()

        composeTestRule.onNodeWithText("${fakeStrings.filePath} (BASE_SCRIPT_PATH)").assertExists()
        composeTestRule.onNodeWithText("${fakeStrings.filePath} (USER_SCRIPT_PATH)").assertExists()

        composeTestRule.onAllNodesWithText(fakeStrings.browseBtn).assertCountEquals(2)

        composeTestRule.onNodeWithText(fakeStrings.saveSettingsBtn).assertExists()
    }

    @Test
    fun `Credentials Tab renders all AWS and OpenStack inputs`() {
        setContent()

        composeTestRule.onNodeWithText(fakeStrings.tabCredentials).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(fakeStrings.awsConfigSection, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText(fakeStrings.openStackConfigSection, ignoreCase = true).assertExists()

        composeTestRule.onNodeWithText("${fakeStrings.awsRegion} (AWS_CLIENT_REGION)").assertExists().performTextInput("eu-west-1")
        composeTestRule.onNodeWithText("${fakeStrings.awsSubnetId} (AWS_SUBNET_ID)").assertExists()

        composeTestRule.onNodeWithText("${fakeStrings.osUserName} (OPENSTACK_USER_NAME)").assertExists()
        composeTestRule.onNodeWithText("${fakeStrings.osPassword} (OPENSTACK_PASSWORD)").assertExists().performTextInput("my-secret-pass")

        composeTestRule.onNodeWithText("${fakeStrings.filePath} (AWS_KEY_FILE_PATH)").assertExists()
        composeTestRule.onNodeWithText("${fakeStrings.filePath} (OPENSTACK_KEY_FILE_PATH)").assertExists()

        composeTestRule.onNodeWithText(fakeStrings.saveSettingsBtn).assertExists()
    }

    @Test
    fun `Save Flow - Clicking Save opens Dialog and Cancel dismisses it`() {
        setContent()

        composeTestRule.onNodeWithText(fakeStrings.tabCredentials).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(fakeStrings.saveSettingsBtn).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(fakeStrings.confirmSaveSettingsTitle).assertExists()
        composeTestRule.onNodeWithText(fakeStrings.confirmSaveSettingsDesc).assertExists()

        composeTestRule.onNodeWithText(fakeStrings.cancelBtn).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(fakeStrings.confirmSaveSettingsTitle).assertDoesNotExist()
    }

    @Test
    fun `Save Flow - Clicking Confirm saves data via Managers`() {
        every { mockState.themeMode } returns ThemeMode.Dark
        every { mockState.currentLanguage } returns Language.EN

        setContent()

        composeTestRule.onNodeWithText(fakeStrings.tabCredentials).performClick()
        composeTestRule.waitForIdle()

        val testRegion = "us-east-2"
        composeTestRule.onNodeWithText("${fakeStrings.awsRegion} (AWS_CLIENT_REGION)").performTextInput(testRegion)

        composeTestRule.onNodeWithText(fakeStrings.saveSettingsBtn).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(fakeStrings.confirmBtn).performClick()
        composeTestRule.waitForIdle()

        verify(exactly = 1) {
            ScriptConfigLoader.saveEnvVariables(
                withArg { map ->
                    assert(map["AWS_CLIENT_REGION"] == testRegion)
                },
            )
        }

        verify(exactly = 1) {
            GeneralSettingsManager.saveSettings("Dark", "EN")
        }

        composeTestRule.onNodeWithText(fakeStrings.confirmSaveSettingsTitle).assertDoesNotExist()
    }
}
