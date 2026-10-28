package com.checkmate.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.checkmate.app.ui.screens.AssignMatesScreen
import com.checkmate.app.ui.screens.DashboardScreen
import com.checkmate.app.ui.screens.EditReceiptScreen
import com.checkmate.app.ui.screens.ScanScreen
import com.checkmate.app.ui.screens.SettleUpScreen
import com.checkmate.app.ui.viewmodel.AppViewModelFactory
import com.checkmate.app.ui.viewmodel.DashboardViewModel
import com.checkmate.app.ui.viewmodel.SplitViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val SCAN = "scan"
    const val EDIT_RECEIPT = "edit_receipt"
    const val ASSIGN_MATES = "assign_mates"
    const val SETTLE_UP = "settle_up"
}

@Composable
fun CheckMateNavHost(navController: NavHostController = rememberNavController()) {
    // One split-in-progress shared by Scan → Edit → Assign → Settle Up (scoped to the activity).
    val splitViewModel: SplitViewModel = viewModel(factory = AppViewModelFactory.Factory)

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory.Factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onScanClick = { navController.navigate(Routes.SCAN) },
            )
        }
        composable(Routes.SCAN) {
            ScanScreen(
                onClose = { navController.popBackStack() },
                onCaptured = {
                    splitViewModel.startNewScan()
                    navController.navigate(Routes.EDIT_RECEIPT)
                },
            )
        }
        composable(Routes.EDIT_RECEIPT) {
            EditReceiptScreen(
                viewModel = splitViewModel,
                onBack = { navController.popBackStack() },
                onConfirm = { navController.navigate(Routes.ASSIGN_MATES) },
            )
        }
        composable(Routes.ASSIGN_MATES) {
            AssignMatesScreen(
                viewModel = splitViewModel,
                onBack = { navController.popBackStack() },
                onCalculate = { navController.navigate(Routes.SETTLE_UP) },
            )
        }
        composable(Routes.SETTLE_UP) {
            SettleUpScreen(
                viewModel = splitViewModel,
                onBack = { navController.popBackStack() },
                onFinished = { navController.popBackStack(Routes.DASHBOARD, inclusive = false) },
            )
        }
    }
}
