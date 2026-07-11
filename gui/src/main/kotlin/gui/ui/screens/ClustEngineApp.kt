package gui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.i18n.LocalStrings
import gui.state.ClustEngineState
import gui.state.enums.AppScreen

@Composable
fun ClustEngineApp(state: ClustEngineState) {
    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isSidebarVisible,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut(),
        ) {
            Sidebar(state)
        }

        Box(modifier = Modifier.weight(1f)) {
            when (state.currentScreen) {
                AppScreen.SavedClusters -> SavedClustersScreen(state)
                AppScreen.Setup -> SetupScreen(state)
                AppScreen.Settings -> SettingsScreen(state)
                AppScreen.Dashboard -> {
                    if (state.activeCluster != null) ClusterDetailsScreen(state) else state.navigateTo(AppScreen.SavedClusters)
                }
                AppScreen.InstanceDetails -> InstanceDetailsScreen(state)
            }
            FloatingMenuButton(
                isVisible = !state.isSidebarVisible,
                onClick = { state.toggleSidebar() },
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 24.dp, start = 24.dp),
            )
        }
    }
}

@Composable
fun FloatingMenuButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = strings.openMenuDesc,
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
fun Sidebar(state: ClustEngineState) {
    val strings = LocalStrings.current

    Column(
        modifier =
            Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colors.surface)
                .padding(24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Cloud, contentDescription = "Logo", tint = MaterialTheme.colors.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("ClustEngine", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { state.toggleSidebar() },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = strings.collapseSidebarDesc,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        SidebarItem(strings.savedClustersMenu, Icons.Default.Storage, state.currentScreen == AppScreen.SavedClusters) {
            state.navigateTo(AppScreen.SavedClusters)
        }

        if (state.activeCluster != null) {
            SidebarItem(strings.dashboardMenu, Icons.Default.Dashboard, state.currentScreen == AppScreen.Dashboard) {
                state.navigateTo(AppScreen.Dashboard)
            }
        }

        SidebarItem(strings.createClusterMenu, Icons.Default.AddCircleOutline, state.currentScreen == AppScreen.Setup) {
            state.navigateToSetup()
        }

        Spacer(modifier = Modifier.weight(1f))

        SidebarItem(strings.settingsTitle, Icons.Default.Settings, state.currentScreen == AppScreen.Settings) {
            state.navigateTo(AppScreen.Settings)
        }
    }
}

@Composable
fun SidebarItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(backgroundColor = bgColor, contentColor = contentColor),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
