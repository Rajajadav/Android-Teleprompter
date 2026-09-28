package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.PromptDeskApp
import com.example.ui.camera.CameraPrompterScreen
import com.example.ui.editor.ScriptEditorScreen
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.scripts.ScriptsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.teleprompter.TeleprompterScreen
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.Teal80
import kotlinx.coroutines.launch

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    BottomNavItem(
        title = "Scripts",
        route = Screen.Scripts.route,
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description,
        testTag = "nav_scripts"
    ),
    BottomNavItem(
        title = "Record",
        route = Screen.CameraRecord.createRoute(0L),
        selectedIcon = Icons.Filled.Videocam,
        unselectedIcon = Icons.Outlined.Videocam,
        testTag = "nav_record"
    ),
    BottomNavItem(
        title = "Settings",
        route = Screen.Settings.route,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
)

@Composable
fun PromptDeskNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val preferencesManager = PromptDeskApp.instance.preferencesManager
    val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = true)
    val coroutineScope = rememberCoroutineScope()

    // Show bottom bar only on core destinations
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Scripts.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = CharcoalSurface,
                    shadowElevation = 8.dp
                ) {
                    NavigationBar(
                        containerColor = CharcoalSurface,
                        contentColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = when (item.route) {
                                Screen.CameraRecord.createRoute(0L) -> currentRoute?.startsWith("camera_record") == true
                                else -> currentRoute == item.route
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (item.route.startsWith("camera_record")) {
                                        navController.navigate(Screen.CameraRecord.createRoute(0L))
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text(item.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF042F2E),
                                    selectedTextColor = Teal80,
                                    indicatorColor = Teal80,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateNext = {
                        val destination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        coroutineScope.launch {
                            preferencesManager.setOnboardingCompleted(true)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToEditor = { scriptId ->
                        navController.navigate(Screen.Editor.createRoute(scriptId))
                    },
                    onNavigateToTeleprompter = { scriptId ->
                        navController.navigate(Screen.Teleprompter.createRoute(scriptId))
                    },
                    onNavigateToCamera = { scriptId ->
                        navController.navigate(Screen.CameraRecord.createRoute(scriptId))
                    },
                    onNavigateToAllScripts = {
                        navController.navigate(Screen.Scripts.route)
                    }
                )
            }

            composable(Screen.Scripts.route) {
                ScriptsScreen(
                    onNavigateToEditor = { scriptId ->
                        navController.navigate(Screen.Editor.createRoute(scriptId))
                    },
                    onNavigateToTeleprompter = { scriptId ->
                        navController.navigate(Screen.Teleprompter.createRoute(scriptId))
                    },
                    onNavigateToCamera = { scriptId ->
                        navController.navigate(Screen.CameraRecord.createRoute(scriptId))
                    }
                )
            }

            composable(
                route = Screen.CameraRecord.route,
                arguments = listOf(
                    navArgument("scriptId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getLong("scriptId") ?: 0L
                CameraPrompterScreen(
                    scriptId = scriptId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(
                    navArgument("scriptId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getLong("scriptId") ?: 0L
                ScriptEditorScreen(
                    scriptId = scriptId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTeleprompter = { targetId ->
                        navController.navigate(Screen.Teleprompter.createRoute(targetId))
                    },
                    onNavigateToCamera = { targetId ->
                        navController.navigate(Screen.CameraRecord.createRoute(targetId))
                    }
                )
            }

            composable(
                route = Screen.Teleprompter.route,
                arguments = listOf(
                    navArgument("scriptId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getLong("scriptId") ?: 0L
                TeleprompterScreen(
                    scriptId = scriptId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
