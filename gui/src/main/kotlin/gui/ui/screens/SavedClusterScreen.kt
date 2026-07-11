package gui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.domain.LiveCluster
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.ui.components.ScreenContainer
import gui.ui.components.ScreenHeader

@Composable
fun SavedClustersScreen(state: ClustEngineState) {
    val strings = LocalStrings.current

    ScreenContainer {
        ScreenHeader(strings.savedClustersTitle, Icons.Default.Storage)

        if (state.savedClusters.isEmpty()) {
            EmptyClustersView(
                onAddClick = { state.navigateToSetup() },
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.savedClusters) { cluster ->
                    ClusterCard(
                        cluster = cluster,
                        onClick = { state.selectAndNavigateToDashboard(cluster) },
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyClustersView(onAddClick: () -> Unit) {
    val strings = LocalStrings.current

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.CloudQueue,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colors.primary.copy(alpha = 0.2f),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = strings.noClustersFoundTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colors.onBackground,
        )
        Text(
            text = strings.createFirstClusterDesc,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onAddClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.height(50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.createNewClusterBtn, color = Color.White)
        }
    }
}

@Composable
fun ClusterCard(
    cluster: LiveCluster,
    onClick: () -> Unit,
) {
    val strings = LocalStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colors.primary)
            }

            Column(modifier = Modifier.padding(start = 20.dp).fillMaxWidth()) {
                Text(
                    text = cluster.clusterName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = strings.clusterNodeCount(cluster.provider, cluster.clusterSize),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
