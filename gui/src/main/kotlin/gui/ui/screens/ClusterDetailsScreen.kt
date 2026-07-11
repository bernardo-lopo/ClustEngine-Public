package gui.ui.screens

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.ui.components.ActionButton
import gui.ui.components.ScreenContainer
import gui.ui.components.ScreenHeader
import gui.ui.components.SectionHeader

@Composable
fun ClusterDetailsScreen(state: ClustEngineState) {
    val cluster = state.activeCluster ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }

    val strings = LocalStrings.current

    ScreenContainer {
        ScreenHeader(title = "${strings.dashboardTitle}: ${cluster.clusterName}", icon = Icons.Default.Dashboard)

        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 0.dp,
            shape = MaterialTheme.shapes.large,
            backgroundColor = MaterialTheme.colors.surface,
        ) {
            val listState = rememberLazyListState()

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(32.dp).fillMaxSize(),
                ) {
                    item {
                        SectionHeader(strings.clusterStatusSection)
                        Text("${strings.providerLabel}: ${cluster.provider}", color = MaterialTheme.colors.onSurface)
                        Text(
                            "${strings.instancesLabel}: ${cluster.clusterSize}",
                            color = MaterialTheme.colors.onSurface,
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        SectionHeader(strings.nodesInformationSection)

                        NodeCard(
                            title = strings.primaryNodeLabel,
                            instance = cluster.primaryInstance,
                            onClick = { state.selectAndNavigateToInstance(cluster.primaryInstance) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    val uniqueSecondaryNodes =
                        cluster.instances
                            .filter { it.id != cluster.primaryInstance.id }
                            .distinctBy { it.id }

                    itemsIndexed(uniqueSecondaryNodes) { index, instance ->
                        NodeCard(
                            title = "${strings.secondaryNodeLabel} ${index + 1}",
                            instance = instance,
                            onClick = { state.selectAndNavigateToInstance(instance) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))

                        SectionHeader(strings.clusterActionsSection)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ActionButton(
                                text = strings.startBtn,
                                icon = Icons.Default.PlayArrow,
                            ) { state.startCluster() }
                            ActionButton(text = strings.stopBtn, icon = Icons.Default.Stop) { state.stopCluster() }

                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.terminateBtn, color = MaterialTheme.colors.error)
                            }
                        }
                    }
                }

                VerticalScrollbar(
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 48.dp),
                    adapter = rememberScrollbarAdapter(listState),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = strings.confirmDeleteClusterTitle)
            },
            text = {
                Text(text = strings.confirmDeleteClusterDesc)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        state.deleteCluster()
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.error,
                            contentColor = MaterialTheme.colors.onError,
                        ),
                ) {
                    Text(strings.confirmBtn)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text(strings.cancelBtn)
                }
            },
        )
    }
}

@Composable
fun NodeCard(
    title: String,
    instance: core.domain.ClustEngineInstance,
    onClick: () -> Unit,
) {
    val strings = LocalStrings.current

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.background,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text("${strings.publicIpLabel}: ${instance.publicIp}", style = MaterialTheme.typography.body2)
                Text("${strings.privateIpLabel}: ${instance.privateIpAddress}", style = MaterialTheme.typography.body2)
                Text(
                    "${strings.instanceIdLabel}: ${instance.id}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = strings.viewDetailsContentDesc)
        }
    }
}
