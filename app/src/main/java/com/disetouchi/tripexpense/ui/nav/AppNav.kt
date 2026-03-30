package com.disetouchi.tripexpense.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.disetouchi.tripexpense.data.repository.RepositoryProvider
import com.disetouchi.tripexpense.ui.screen.expenses.EditExpenseScreen
import com.disetouchi.tripexpense.ui.screen.home.HomeScreen
import com.disetouchi.tripexpense.ui.screen.home.HomeViewModel
import com.disetouchi.tripexpense.ui.screen.home.HomeViewModelFactory
import com.disetouchi.tripexpense.ui.screen.trip.EditTripScreen
import com.disetouchi.tripexpense.ui.screen.trip.TripDetailScreen

/**
 * App Navigation host.
 * Also manages app-level lifecycle observations such as auto-sync on foreground return.
 */
@Composable
fun AppNav() {
    val navController = rememberNavController()

    // Initialize HomeViewModel at the AppNav level to ensure consistency across screens
    // and for global lifecycle-triggered actions like auto-sync.
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            tripRepository = RepositoryProvider.tripRepository,
            expenseRepository = RepositoryProvider.expenseRepository,
            rateSnapshotRepository = RepositoryProvider.rateSnapshotRepository,
            userPreferencesRepository = RepositoryProvider.userPreferencesRepository
        )
    )

    // Observe application process lifecycle (foreground return)
    DisposableEffect(ProcessLifecycleOwner.get()) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Request auto-sync when the app returns to the foreground
                homeViewModel.requestAutoSync()
            }
        }
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onTripClick = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
                onAddTripClick = {
                    navController.navigate(Screen.AddTrip.route)
                },
                onAddExpenseClick = { tripId ->
                    navController.navigate(Screen.AddExpense.createRoute(tripId))
                },
                onExpenseCardClick = { tripId, expenseId ->
                    navController.navigate(
                        Screen.ExpenseDetail.createRoute(
                            tripId = tripId,
                            expenseId = expenseId
                        )
                    )
                }
            )
        }

        // TripDetail
        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L

            TripDetailScreen(
                tripId = tripId,
                onBackClick = { navController.popBackStack() },
                onEditTripClick = { tripId ->
                    navController.navigate(Screen.EditTrip.createRoute(tripId))
                },
                onDeleteTripClick = { id ->
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onExpenseDetailClick = { tripId, expenseId ->
                    navController.navigate(
                        Screen.ExpenseDetail.createRoute(
                            tripId = tripId,
                            expenseId = expenseId
                        )
                    )
                }
            )
        }

        // AddTrip
        composable(Screen.AddTrip.route) {
            EditTripScreen(
                tripId = null,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        // EditTrip
        composable(
            route = Screen.EditTrip.route,
            arguments = listOf(navArgument(Screen.EditTrip.ARG_TRIP_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong(Screen.EditTrip.ARG_TRIP_ID) ?: 0L
            EditTripScreen(
                tripId = tripId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        // ExpenseDetail
        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(
                navArgument(Screen.ExpenseDetail.ARG_TRIP_ID) {
                    type = NavType.LongType
                },
                navArgument(Screen.ExpenseDetail.ARG_EXPENSE_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val expenseId =
                requireNotNull(backStackEntry.arguments?.getLong(Screen.ExpenseDetail.ARG_EXPENSE_ID))
            val tripId =
                requireNotNull(backStackEntry.arguments?.getLong(Screen.ExpenseDetail.ARG_TRIP_ID))
            com.disetouchi.tripexpense.ui.screen.expenses.ExpenseDetailScreen(
                expenseId = expenseId,
                tripId = tripId,
                onBackClick = { navController.popBackStack() },
                onEditExpenseClick = { expenseId ->
                    navController.navigate(
                        Screen.EditExpense.createRoute(tripId, expenseId)
                    )
                },
                onDeleteExpenseClick = { expenseId ->
                    navController.popBackStack()
                }
            )
        }

        // AddExpense
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument(Screen.AddExpense.ARG_TRIP_ID) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val tripId =
                requireNotNull(backStackEntry.arguments?.getLong(Screen.AddExpense.ARG_TRIP_ID))
            EditExpenseScreen(
                tripId = tripId,
                expenseId = null,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        // EditExpense
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument(Screen.EditExpense.ARG_TRIP_ID) { type = NavType.LongType },
                navArgument(Screen.EditExpense.ARG_EXPENSE_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId =
                requireNotNull(backStackEntry.arguments?.getLong(Screen.EditExpense.ARG_TRIP_ID))
            val expenseId =
                requireNotNull(backStackEntry.arguments?.getLong(Screen.EditExpense.ARG_EXPENSE_ID))

            EditExpenseScreen(
                tripId = tripId,
                expenseId = expenseId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
    }
}
