package com.disetouchi.tripexpense.ui.nav

/**
 * Navigation routes (single source of truth)
 */
sealed class Screen(val route: String) {
    // Home
    data object Home : Screen("home")

    // Trip Detail
    data object TripDetail : Screen("trip/detail/{tripId}") {
        fun createRoute(tripId: Long) = "trip/detail/$tripId"
        const val ARG_TRIP_ID = "tripId"
    }

    // Edit Trip
    data object AddTrip : Screen("trip/add")
    data object EditTrip : Screen("trip/edit/{tripId}") {
        fun createRoute(tripId: Long) = "trip/edit/$tripId"
        const val ARG_TRIP_ID = "tripId"
    }

    // Expense Detail
    data object ExpenseDetail : Screen("expense/detail/{tripId}/{expenseId}") {
        fun createRoute(tripId: Long, expenseId: Long) = "expense/detail/$tripId/$expenseId"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_EXPENSE_ID = "expenseId"
    }

    // Edit Expense
    data object AddExpense : Screen("expense/add/{tripId}") {
        fun createRoute(tripId: Long) = "expense/add/$tripId"
        const val ARG_TRIP_ID = "tripId"
    }

    data object EditExpense : Screen("expense/edit/{tripId}/{expenseId}") {
        fun createRoute(tripId: Long, expenseId: Long) = "expense/edit/$tripId/$expenseId"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_EXPENSE_ID = "expenseId"
    }

}