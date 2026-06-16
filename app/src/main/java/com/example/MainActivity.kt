package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProfileWizardScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.TutorialScreen
import com.example.ui.theme.FocusDeckTheme
import com.example.viewmodel.MainViewModel
import com.example.notifications.NotificationHelper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannels(this)
        setContent {
            val gamificationState by viewModel.gamificationState.collectAsState()
            
            FocusDeckTheme(themeName = gamificationState.currentTheme) {
                val navController = rememberNavController()
                
                androidx.compose.runtime.LaunchedEffect(
                    gamificationState.isProfileSetup,
                    gamificationState.tutorialCompleted
                ) {
                    if (gamificationState.isProfileSetup) {
                        val dest = navController.currentDestination?.route
                        if (dest != "dashboard" && dest != "tutorial") {
                            if (!gamificationState.tutorialCompleted) {
                                com.example.notifications.ReminderScheduler
                                    .rescheduleAll(this@MainActivity, gamificationState)
                                navController.navigate("tutorial") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                com.example.notifications.ReminderScheduler
                                    .rescheduleAll(this@MainActivity, gamificationState)
                                navController.navigate("dashboard") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }
                
                NavHost(
                    navController = navController,
                    startDestination = "profile"
                ) {
                    composable("profile") {
                        ProfileWizardScreen(
                            viewModel = viewModel,
                            onComplete = {
                                if (!gamificationState.tutorialCompleted) {
                                    navController.navigate("tutorial") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("dashboard") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                    
                    composable("tutorial") {
                        TutorialScreen(
                            viewModel = viewModel,
                            onComplete = {
                                navController.navigate("dashboard") {
                                    popUpTo("tutorial") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToShop = { navController.navigate("shop") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToBadges = { navController.navigate("badges") }
                        )
                    }
                    
                    composable("shop") {
                        ShopScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable("badges") {
                        com.example.ui.screens.BadgesScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        com.example.ui.screens.SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onEditProfile = {
                                navController.navigate("profile") {
                                    popUpTo("settings") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
