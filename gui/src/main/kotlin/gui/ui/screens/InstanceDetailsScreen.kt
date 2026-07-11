package gui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.state.enums.AppScreen
import gui.ui.components.ActionButton
import gui.ui.components.ScreenContainer
import gui.ui.components.ScreenHeader
import gui.ui.components.SectionHeader

@Composable
fun InstanceDetailsScreen(state: ClustEngineState) {
    val instance = state.activeInstance ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }

    val strings = LocalStrings.current

    ScreenContainer {
        TextButton(
            onClick = { state.navigateTo(AppScreen.Dashboard) },
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.backToDashboard)
        }

        ScreenHeader(title = strings.nodeManagementTitle, icon = Icons.Default.Computer)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 0.dp,
            shape = MaterialTheme.shapes.large,
            backgroundColor = MaterialTheme.colors.surface,
        ) {
            Column(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                SectionHeader(strings.instanceDetailsSection)

                DetailRow(strings.instanceIdLabel, instance.id)
                DetailRow(strings.publicIpLabel, instance.publicIp)
                DetailRow(strings.privateIpLabel, instance.privateIpAddress)
                DetailRow(strings.publicDnsLabel, instance.publicDns.ifBlank { "N/A" })

                Spacer(modifier = Modifier.height(40.dp))

                SectionHeader(strings.nodeActionsSection)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionButton(text = strings.startNodeBtn, icon = Icons.Default.PlayArrow) {
                        state.startInstance(instance.id)
                    }
                    ActionButton(text = strings.stopNodeBtn, icon = Icons.Default.Stop) {
                        state.stopInstance(instance.id)
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.terminateNodeBtn)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.terminateInstanceDialogTitle) },
            text = { Text(strings.terminateInstanceDialogDesc(instance.id)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        state.deleteInstance(instance.id)
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.error,
                            contentColor = MaterialTheme.colors.onError,
                        ),
                ) {
                    Text(strings.terminateBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancelBtn)
                }
            },
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface)

        SelectionContainer {
            Text(value, color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f))
        }

        if (value.isNotBlank() && value != "N/A") {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
