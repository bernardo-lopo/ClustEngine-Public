package gui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import core.util.ScriptConfigLoader
import gui.i18n.Language
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.state.enums.ThemeMode
import gui.ui.components.ScreenContainer
import gui.ui.components.ScreenHeader
import gui.ui.components.SectionHeader

@Composable
fun SettingsScreen(state: ClustEngineState) {
    val strings = LocalStrings.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    // 0 = General, 1 = Scripts, 2 = Credentials
    var selectedTab by remember { mutableStateOf(0) }

    val formState =
        remember {
            val keys =
                listOf(
                    "BASE_SCRIPT_PATH",
                    "BASE_SCRIPT_NAME",
                    "USER_SCRIPT_PATH",
                    "USER_SCRIPT_NAME",
                    "AWS_CLIENT_REGION",
                    "AWS_KEY_FILE_NAME",
                    "AWS_KEY_FILE_PATH",
                    "AWS_SUBNET_ID",
                    "AWS_SECURITY_GROUP_ID",
                    "AWS_IMAGE_ID",
                    "OPENSTACK_USER_NAME",
                    "OPENSTACK_DOMAIN",
                    "OPENSTACK_PASSWORD",
                    "OPENSTACK_PROJECT_NAME",
                    "OPENSTACK_BASE_URL",
                    "OPENSTACK_KEY_FILE_NAME",
                    "OPENSTACK_KEY_FILE_PATH",
                    "OPENSTACK_IMAGE_ID",
                    "OPENSTACK_AVAILABILITY_ZONE",
                    "OPENSTACK_SECURITY_GROUP",
                    "OPENSTACK_NETWORK_ID",
                )
            mutableStateMapOf<String, String>().apply {
                keys.forEach { key ->
                    this[key] = ScriptConfigLoader.getEnvOrNull(key)
                }
            }
        }

    ScreenContainer {
        ScreenHeader(title = strings.settingsTitle, icon = Icons.Default.Settings)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(strings.tabGeneral) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(strings.tabScriptInjection) },
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(strings.tabCredentials) },
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (selectedTab) {
                        0 -> GeneralSettingsTab(state)
                        1 -> ScriptInjectionTab(formState, strings.scriptConfigSection)
                        2 -> CredentialsTab(formState, strings.awsConfigSection, strings.openStackConfigSection)
                    }
                }

                Divider()
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.saveSettingsBtn)
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(strings.confirmSaveSettingsTitle) },
            text = { Text(strings.confirmSaveSettingsDesc) },
            confirmButton = {
                Button(
                    onClick = {
                        ScriptConfigLoader.saveEnvVariables(formState)

                        core.util.GeneralSettingsManager.saveSettings(
                            theme = state.themeMode.name,
                            language = state.currentLanguage.name,
                        )

                        showConfirmDialog = false
                    },
                ) {
                    Text(strings.confirmBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(strings.cancelBtn)
                }
            },
        )
    }
}

@Composable
private fun GeneralSettingsTab(state: ClustEngineState) {
    val strings = LocalStrings.current

    Text(strings.settingsTitle, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    // Theme Settings
    SectionHeader(strings.themeLabel)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ThemeMode.entries.forEach { mode ->
            val isSelected = state.themeMode == mode
            val label =
                when (mode) {
                    ThemeMode.Light -> strings.themeLight
                    ThemeMode.Dark -> strings.themeDark
                    ThemeMode.Auto -> strings.themeAuto
                }
            SettingsChip(label, isSelected) { state.themeMode = mode }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Language Settings
    SectionHeader(strings.languageLabel)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Language.entries.forEach { lang ->
            val isSelected = state.currentLanguage == lang
            val label =
                when (lang) {
                    Language.Auto -> strings.themeAuto
                    else -> lang.name
                }

            SettingsChip(label, isSelected) { state.currentLanguage = lang }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // About and Documentation
    SectionHeader(strings.versionLabel)
    Text(text = "ClustEngine v${state.appVersion}", color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = { state.openDocumentation() },
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(strings.docsLabel)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SettingsChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors =
            ButtonDefaults.buttonColors(
                backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                contentColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
            ),
        elevation = ButtonDefaults.elevation(0.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(label)
    }
}

@Composable
private fun ScriptInjectionTab(
    formState: MutableMap<String, String>,
    sectionTitle: String,
) {
    val strings = LocalStrings.current

    SectionHeader(sectionTitle)
    Spacer(modifier = Modifier.height(8.dp))

    FilePickerField(
        title = strings.baseScriptConfig,
        label = "${strings.filePath} (BASE_SCRIPT_PATH)",
        pathKey = "BASE_SCRIPT_PATH",
        nameKey = "BASE_SCRIPT_NAME",
        formState = formState,
    )

    Spacer(modifier = Modifier.height(16.dp))

    FilePickerField(
        title = strings.userScriptConfig,
        label = "${strings.filePath} (USER_SCRIPT_PATH)",
        pathKey = "USER_SCRIPT_PATH",
        nameKey = "USER_SCRIPT_NAME",
        formState = formState,
    )
}

@Composable
private fun CredentialsTab(
    formState: MutableMap<String, String>,
    awsTitle: String,
    osTitle: String,
) {
    val strings = LocalStrings.current

    // AWS Section
    SectionHeader(awsTitle)
    ConfigTextField("${strings.awsRegion} (AWS_CLIENT_REGION)", formState, "AWS_CLIENT_REGION")
    ConfigTextField("${strings.awsSubnetId} (AWS_SUBNET_ID)", formState, "AWS_SUBNET_ID")
    ConfigTextField("${strings.awsSecGroupId} (AWS_SECURITY_GROUP_ID)", formState, "AWS_SECURITY_GROUP_ID")
    ConfigTextField("${strings.awsImageId} (AWS_IMAGE_ID)", formState, "AWS_IMAGE_ID")

    Spacer(modifier = Modifier.height(8.dp))

    FilePickerField(
        title = strings.awsKeyFile,
        label = "${strings.filePath} (AWS_KEY_FILE_PATH)",
        pathKey = "AWS_KEY_FILE_PATH",
        nameKey = "AWS_KEY_FILE_NAME",
        formState = formState,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // OpenStack Section
    SectionHeader(osTitle)
    ConfigTextField("${strings.osUserName} (OPENSTACK_USER_NAME)", formState, "OPENSTACK_USER_NAME")
    ConfigTextField("${strings.osDomain} (OPENSTACK_DOMAIN)", formState, "OPENSTACK_DOMAIN")
    ConfigTextField("${strings.osPassword} (OPENSTACK_PASSWORD)", formState, "OPENSTACK_PASSWORD", isPassword = true)
    ConfigTextField("${strings.osProjectName} (OPENSTACK_PROJECT_NAME)", formState, "OPENSTACK_PROJECT_NAME")
    ConfigTextField("${strings.osBaseUrl} (OPENSTACK_BASE_URL)", formState, "OPENSTACK_BASE_URL")
    ConfigTextField("${strings.osImageId} (OPENSTACK_IMAGE_ID)", formState, "OPENSTACK_IMAGE_ID")
    ConfigTextField("${strings.osAvailabilityZone} (OPENSTACK_AVAILABILITY_ZONE)", formState, "OPENSTACK_AVAILABILITY_ZONE")
    ConfigTextField("${strings.osSecurityGroup} (OPENSTACK_SECURITY_GROUP)", formState, "OPENSTACK_SECURITY_GROUP")
    ConfigTextField("${strings.osNetworkId} (OPENSTACK_NETWORK_ID)", formState, "OPENSTACK_NETWORK_ID")

    Spacer(modifier = Modifier.height(8.dp))

    FilePickerField(
        title = strings.osKeyFile,
        label = "${strings.filePath} (OPENSTACK_KEY_FILE_PATH)",
        pathKey = "OPENSTACK_KEY_FILE_PATH",
        nameKey = "OPENSTACK_KEY_FILE_NAME",
        formState = formState,
    )
}

@Composable
private fun FilePickerField(
    title: String,
    label: String,
    pathKey: String,
    nameKey: String,
    formState: MutableMap<String, String>,
) {
    val strings = LocalStrings.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)

                OutlinedButton(
                    onClick = {
                        val dialog = java.awt.FileDialog(null as java.awt.Frame?, strings.selectFileDialogTitle, java.awt.FileDialog.LOAD)
                        dialog.isVisible = true

                        if (dialog.directory != null && dialog.file != null) {
                            val dir = dialog.directory.replace("\\", "/")
                            val fullPath = if (dir.endsWith("/")) dir + dialog.file else "$dir/${dialog.file}"

                            formState[pathKey] = fullPath
                            formState[nameKey] = dialog.file
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Folder, contentDescription = strings.browseBtn, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.browseBtn)
                }
            }

            ConfigTextField(label, formState, pathKey, readOnly = true)
        }
    }
}

@Composable
private fun ConfigTextField(
    label: String,
    stateMap: MutableMap<String, String>,
    key: String,
    isPassword: Boolean = false,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        value = stateMap[key] ?: "",
        onValueChange = { if (!readOnly) stateMap[key] = it },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = readOnly,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    )
}
