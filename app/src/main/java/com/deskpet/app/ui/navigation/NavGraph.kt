package com.deskpet.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deskpet.app.ui.components.BottomNav
import com.deskpet.app.ui.components.PetDestinations
import com.deskpet.app.ui.screens.dressup.DressUpScreen
import com.deskpet.app.ui.screens.health.HealthScreen
import com.deskpet.app.ui.screens.home.PetHomeScreen
import com.deskpet.app.ui.screens.onboarding.OnboardingScreen
import com.deskpet.app.ui.screens.settings.SettingsScreen

/**
 * Top-level navigation graph.
 *
 * Routes: onboarding → home → {dressup / health / settings} (bottom nav).
 * The bottom navigation bar is shown on every tab route but hidden on
 * onboarding.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Routes that should display the bottom navigation bar.
    val showBottomBar = currentRoute in setOf(
        PetDestinations.HOME,
        PetDestinations.DRESSUP,
        PetDestinations.HEALTH,
        PetDestinations.SETTINGS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large back stack.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PetDestinations.ONBOARDING,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(PetDestinations.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(PetDestinations.HOME) {
                            popUpTo(PetDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(PetDestinations.HOME) {
                PetHomeScreen(
                    onNavigateToDressUp = {
                        navController.navigate(PetDestinations.DRESSUP) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(PetDestinations.DRESSUP) {
                DressUpScreen()
            }
            composable(PetDestinations.HEALTH) {
                HealthScreen()
            }
            composable(PetDestinations.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
