package gui.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import core.util.ClustEngineInstanceTypeLoader
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.state.enums.AppScreen
import gui.state.enums.CloudProvider
import gui.state.enums.UIRoutingMode
import gui.ui.components.ActionButton
import gui.ui.components.ConsoleTerminal
import gui.ui.components.ScreenContainer
import gui.ui.components.ScreenHeader
import gui.ui.components.SectionHeader

val SIZE_PRESETS = listOf(2, 4, 8, 16, 32, 64, 128, 256, 512)

@Composable
fun SetupScreen(state: ClustEngineState) {
    val strings = LocalStrings.current

    var showCancelDialog by remember { mutableStateOf(false) }

    val sizeInt = state.clusterSize.toIntOrNull() ?: 0
    val isFormValid =
        state.clusterName.isNotBlank() &&
            sizeInt in 1..512 &&
            state.selectedProvider != null &&
            state.selectedFlavor != null &&
            state.selectedRoutingMode != null

    ScreenContainer {
        ScreenHeader(title = strings.setupTitle, icon = Icons.Default.AddCircleOutline)

        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.surface,
            shape = MaterialTheme.shapes.large,
        ) {
            val scrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier =
                        Modifier
                            .padding(40.dp)
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                ) {
                    SectionHeader(strings.clusterDetailsSection)
                    OutlinedTextField(
                        value = state.clusterName,
                        onValueChange = { state.clusterName = it },
                        label = { Text("Cluster Name") },
                        placeholder = { Text("e.g. production-cluster-01") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors =
                            TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                            ),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    SectionHeader(strings.cloudProviderTitle)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProviderTextCard(
                            providerName = "AWS",
                            isSelected = state.selectedProvider == CloudProvider.AWS,
                            modifier = Modifier.weight(1f),
                        ) {
                            state.selectedProvider = CloudProvider.AWS
                            val flavors = ClustEngineInstanceTypeLoader.loadClustEngineInstanceType("config/aws_instances.json")
                            state.availableFlavors = flavors
                            state.selectedFlavor = flavors.firstOrNull()
                        }

                        ProviderTextCard(
                            providerName = "OpenStack",
                            isSelected = state.selectedProvider == CloudProvider.OpenStack,
                            modifier = Modifier.weight(1f),
                        ) {
                            state.selectedProvider = CloudProvider.OpenStack
                            val flavors = ClustEngineInstanceTypeLoader.loadClustEngineInstanceType("config/openstack_flavors.json")
                            state.availableFlavors = flavors
                            state.selectedFlavor = flavors.firstOrNull()
                        }
                    }

                    if (state.selectedProvider != null && state.availableFlavors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        SectionHeader(strings.instanceFlavorTitle)

                        var expanded by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value =
                                    state.selectedFlavor?.let {
                                        "${it.displayName}  |  ${it.specs}"
                                    } ?: strings.selectFlavorPlaceholder,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors =
                                    TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                    ),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        Modifier.clickable { expanded = true },
                                    )
                                },
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .background(Color.Transparent)
                                        .clickable { expanded = true },
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f),
                            ) {
                                state.availableFlavors.forEach { flavor ->
                                    DropdownMenuItem(onClick = {
                                        state.selectedFlavor = flavor
                                        expanded = false
                                    }) {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(flavor.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                                            Text(flavor.specs, style = MaterialTheme.typography.caption, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader("Cluster Size (Max 512 Nodes)")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = state.clusterSize,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                val intValue = filtered.toIntOrNull()

                                if (filtered.isEmpty()) {
                                    state.clusterSize = ""
                                } else if ((intValue != null) && (intValue <= 512)) {
                                    state.clusterSize = intValue.toString()
                                } else if (intValue != null) {
                                    state.clusterSize = "512"
                                }
                            },
                            label = { Text("Nodes") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        val sliderValue = (state.clusterSize.toFloatOrNull() ?: 1f).coerceIn(1f, 512f)
                        Slider(
                            value = sliderValue,
                            onValueChange = { state.clusterSize = it.toInt().toString() },
                            valueRange = 1f..512f,
                            modifier = Modifier.weight(1f),
                            colors =
                                SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colors.primary,
                                    activeTrackColor = MaterialTheme.colors.primary,
                                ),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SIZE_PRESETS.forEach { preset ->
                            PresetSizeChip(
                                label = preset.toString(),
                                isSelected = state.clusterSize == preset.toString(),
                            ) {
                                state.clusterSize = preset.toString()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader("Network Routing Mode")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProviderTextCard(
                            providerName = "Single IP",
                            isSelected = state.selectedRoutingMode == UIRoutingMode.SINGLE_IP,
                            modifier = Modifier.weight(1f),
                        ) {
                            state.selectedRoutingMode = UIRoutingMode.SINGLE_IP
                        }

                        ProviderTextCard(
                            providerName = "Multi IP",
                            isSelected = state.selectedRoutingMode == UIRoutingMode.MULTI_IP,
                            modifier = Modifier.weight(1f),
                        ) {
                            state.selectedRoutingMode = UIRoutingMode.MULTI_IP
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    if (state.isClusterBeingCreated) {
                        // Format the seconds into MM:SS
                        val minutes = state.creationTimeInSeconds / 60
                        val seconds = state.creationTimeInSeconds % 60
                        val timeString = String.format("%02d:%02d", minutes, seconds)

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colors.primary,
                                    strokeWidth = 3.dp,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Creating cluster... $timeString",
                                    color = MaterialTheme.colors.primary,
                                    style = MaterialTheme.typography.body1,
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text = strings.cancelClusterCreationBtn,
                                    color = MaterialTheme.colors.error,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    if (state.isCreationSuccess) {
                        ActionButton(
                            text = strings.goToClustersBtn,
                            icon = Icons.Default.CheckCircle,
                            enabled = true,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            state.navigateTo(AppScreen.SavedClusters)
                        }
                    } else {
                        ActionButton(
                            text = if (state.isClusterBeingCreated) strings.deployingBtn else strings.initEngineBtn,
                            icon = Icons.Default.RocketLaunch,
                            enabled = isFormValid && !state.isClusterBeingCreated,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            state.initEngine()
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    ConsoleTerminal(
                        logs = state.consoleLogs,
                        title = strings.terminalTitle,
                        emptyMessage = strings.terminalEmpty,
                        modifier = Modifier.height(250.dp),
                    )
                }

                VerticalScrollbar(
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 48.dp),
                    adapter = rememberScrollbarAdapter(scrollState),
                )
            }
        }
    }
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(text = strings.confirmClusterCancelCreationTitle)
            },
            text = {
                Text(text = strings.confirmCancelCreationDesc)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        state.cancelClusterCreation()
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.error,
                            contentColor = MaterialTheme.colors.onError,
                        ),
                ) {
                    Text(strings.cancelClusterCreationBtn)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false },
                ) {
                    Text(strings.cancelBtn)
                }
            },
        )
    }
}

@Composable
fun ProviderTextCard(
    providerName: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.1f)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = bgColor),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = providerName, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PresetSizeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
    val contentColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface.copy(alpha = 0.8f)

    Button(
        onClick = onClick,
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = bgColor, contentColor = contentColor),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        modifier = Modifier.height(36.dp),
    ) {
        Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}
