package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object ProfileSetup : Screen("profile_setup")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Scripts : Screen("scripts")
    object CameraRecord : Screen("camera_record/{scriptId}") {
        fun createRoute(scriptId: Long = 0L) = "camera_record/$scriptId"
    }
    object Settings : Screen("settings")
    object Editor : Screen("editor/{scriptId}") {
        fun createRoute(scriptId: Long = 0L) = "editor/$scriptId"
    }
    object Teleprompter : Screen("teleprompter/{scriptId}") {
        fun createRoute(scriptId: Long) = "teleprompter/$scriptId"
    }
}
