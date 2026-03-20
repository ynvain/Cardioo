package com.cardioo.presentation.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cardioo.presentation.accounts.AccountsScreen
import com.cardioo.presentation.chart.ChartScreen
import com.cardioo.presentation.entry.MeasurementEntryScreen
import com.cardioo.presentation.main.MainScaffold
import com.cardioo.presentation.onboarding.OnboardingScreen
import com.cardioo.presentation.readings.ReadingsScreen
import com.cardioo.presentation.settings.SettingsScreen
import com.cardioo.presentation.splash.SplashScreen

@Composable
fun CardiooRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
    ) {
        composable(Routes.Splash) {
            // Splash decides the "real" start destination based on whether the profile exists.
            SplashScreen(
                onGoToOnboarding = {
                    navController.navigate(Routes.Onboarding) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onGoToMain = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Onboarding) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Main) {
            // Bottom-nav "Main" is a single destination; the tabs are internal state to keep the
            // app structure simple (exactly two bottom items as requested).
            MainScaffold(
                onOpenEntry = { idOrNull ->
                    val route = if (idOrNull == null) Routes.Entry else "${Routes.Entry}?${Routes.ArgMeasurementId}=$idOrNull"
                    navController.navigate(route)
                },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenManageAccounts = { navController.navigate(Routes.Accounts) },
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Accounts) {
            AccountsScreen(
                onBack = { navController.popBackStack() },
                onEditCurrent = { navController.navigate(Routes.Settings) },
            )
        }

        composable(
            route = "${Routes.Entry}?${Routes.ArgMeasurementId}={${Routes.ArgMeasurementId}}",
            arguments = listOf(
                navArgument(Routes.ArgMeasurementId) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Routes.ArgMeasurementId) ?: -1L
            MeasurementEntryScreen(
                measurementId = id.takeIf { it > 0L },
                onDone = { navController.popBackStack() },
            )
        }
    }
}

