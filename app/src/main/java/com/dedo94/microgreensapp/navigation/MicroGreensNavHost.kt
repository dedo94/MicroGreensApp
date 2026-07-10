package com.dedo94.microgreensapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dedo94.microgreensapp.feature.calendar.CalendarScreen
import com.dedo94.microgreensapp.feature.event.EventEditScreen
import com.dedo94.microgreensapp.feature.settings.SettingsScreen
import com.dedo94.microgreensapp.feature.stats.StatsScreen
import com.dedo94.microgreensapp.feature.template.TemplateEditScreen
import com.dedo94.microgreensapp.feature.template.TemplateListScreen
import com.dedo94.microgreensapp.feature.tray.TrayCreateScreen
import com.dedo94.microgreensapp.feature.tray.TrayDetailScreen
import com.dedo94.microgreensapp.feature.tray.TrayEditScreen
import com.dedo94.microgreensapp.feature.tray.TraysListScreen

@Composable
fun MicroGreensNavHost(
    deepLinkTrayId: MutableState<Long?> = remember { mutableStateOf(null) },
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    LaunchedEffect(deepLinkTrayId.value) {
        val trayId = deepLinkTrayId.value
        if (trayId != null) {
            navController.navigate(TrayDetailRoute(trayId))
            deepLinkTrayId.value = null
        }
    }

    fun navigateToTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<CalendarRoute>() } == true,
                    onClick = { navigateToTopLevel(CalendarRoute) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<TraysListRoute>() } == true,
                    onClick = { navigateToTopLevel(TraysListRoute) },
                    icon = { Icon(Icons.Default.Eco, contentDescription = "Vassoi") },
                    label = { Text("Vassoi") },
                )
                FilledIconButton(
                    onClick = { navController.navigate(TrayCreateRoute) },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(48.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuovo vassoio")
                }
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<StatsRoute>() } == true,
                    onClick = { navigateToTopLevel(StatsRoute) },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Statistiche") },
                    label = { Text("Statistiche") },
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<SettingsRoute>() } == true,
                    onClick = { navigateToTopLevel(SettingsRoute) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Opzioni") },
                    label = { Text("Opzioni") },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = CalendarRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<CalendarRoute> {
                CalendarScreen(onOpenTray = { id -> navController.navigate(TrayDetailRoute(id)) })
            }
            composable<TraysListRoute> {
                TraysListScreen(
                    onOpenTray = { id -> navController.navigate(TrayDetailRoute(id)) },
                )
            }
            composable<StatsRoute> { StatsScreen() }
            composable<SettingsRoute> {
                SettingsScreen(
                    onManageVarieties = { navController.navigate(TemplateListRoute) },
                )
            }

            composable<TrayCreateRoute> {
                TrayCreateScreen(
                    onBack = { navController.popBackStack() },
                    onManageVarieties = { navController.navigate(TemplateListRoute) },
                    onCreated = { id ->
                        navController.navigate(TrayDetailRoute(id)) {
                            popUpTo<TrayCreateRoute> { inclusive = true }
                        }
                    },
                )
            }
            composable<TrayDetailRoute> {
                TrayDetailScreen(
                    onBack = { navController.popBackStack() },
                    onAddEvent = { trayId -> navController.navigate(EventEditRoute(trayId = trayId)) },
                    onEditEvent = { trayId, eventId ->
                        navController.navigate(EventEditRoute(trayId = trayId, eventId = eventId))
                    },
                    onEditTray = { trayId -> navController.navigate(TrayEditRoute(trayId)) },
                )
            }
            composable<TrayEditRoute> {
                TrayEditScreen(onBack = { navController.popBackStack() })
            }
            composable<EventEditRoute> {
                EventEditScreen(onBack = { navController.popBackStack() })
            }

            composable<TemplateListRoute> {
                TemplateListScreen(
                    onCreateTemplate = { navController.navigate(TemplateEditRoute(templateId = 0L)) },
                    onOpenTemplate = { id -> navController.navigate(TemplateEditRoute(templateId = id)) },
                )
            }
            composable<TemplateEditRoute> {
                TemplateEditScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
