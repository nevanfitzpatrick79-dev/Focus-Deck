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
import com.example.ui.theme.FocusDeckTheme
import com.example.viewmodel.MainViewModel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gamificationState by viewModel.gamificationState.collectAsState()
            
            FocusDeckTheme(themeName = gamificationState.currentTheme) {
                val navController = rememberNavController()
                
                androidx.compose.runtime.LaunchedEffect(gamificationState.isProfileSetup) {
                    if (gamificationState.isProfileSetup && navController.currentDestination?.route != "dashboard") {
                        navController.navigate("dashboard") {
                            popUpTo(0) { inclusive = true }
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
                                navController.navigate("dashboard") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToShop = { navController.navigate("shop") }
                        )
                    }
                    
                    composable("shop") {
                        ShopScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
