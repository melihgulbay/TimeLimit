package com.example.timelimit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timelimit.feature.focus.FocusFeedScreen
import com.example.timelimit.feature.dashboard.MainScreen
import com.example.timelimit.feature.onboarding.OnboardingManager
import com.example.timelimit.feature.onboarding.OnboardingScreen
import com.example.timelimit.feature.onboarding.permission.PermissionScreen
import com.example.timelimit.feature.settings.SettingsScreen
import com.example.timelimit.feature.statistics.StatisticsScreen
import com.example.timelimit.core.ui.theme.TimeLimitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeLimitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimeLimitApp()
                }
            }
        }
    }
}

@Composable
fun TimeLimitApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Check onboarding status
    val onboardingManager = remember { OnboardingManager(context) }
    val isOnboardingCompleted by onboardingManager.isOnboardingCompleted.collectAsState(initial = null)

    // Determine start destination
    val startDestination = when {
        isOnboardingCompleted == null -> "loading"
        isOnboardingCompleted == false -> "onboarding"
        else -> "permission"
    }

    if (isOnboardingCompleted == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    scope.launch {
                        onboardingManager.setOnboardingCompleted()
                        navController.navigate("permission") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("permission") { 
            PermissionScreen(navController = navController) 
        }
        composable(
            "main",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { 
            MainScreen(navController = navController) 
        }
        composable("settings") { 
            SettingsScreen() 
        }
        composable("statistics") { 
            StatisticsScreen(navController = navController) 
        }
        composable("focus_feed") {
            FocusFeedScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
