package com.disetouchi.tripexpense.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class ExpenseCategory(
    val categoryId: Int,
    val categoryName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Food : ExpenseCategory(
        categoryId = 1,
        categoryName = "Food",
        icon = Icons.Filled.Restaurant
    )

    data object Cafe : ExpenseCategory(
        categoryId = 2,
        categoryName = "Cafe",
        icon = Icons.Filled.LocalCafe
    )

    data object Groceries : ExpenseCategory(
        categoryId = 3,
        categoryName = "Groceries",
        icon = Icons.Filled.ShoppingCart
    )

    data object Transport : ExpenseCategory(
        categoryId = 4,
        categoryName = "Transport",
        icon = Icons.Filled.DirectionsTransit
    )

    data object Sightseeing : ExpenseCategory(
        categoryId = 5,
        categoryName = "Sightseeing",
        icon = Icons.Filled.Museum
    )

    data object Shopping : ExpenseCategory(
        categoryId = 6,
        categoryName = "Shopping",
        icon = Icons.Filled.ShoppingBag
    )

    data object Hotel : ExpenseCategory(
        categoryId = 7,
        categoryName = "Hotel",
        icon = Icons.Filled.Hotel
    )

    data object Sim : ExpenseCategory(
        categoryId = 8,
        categoryName = "SIM & Internet",
        icon = Icons.Filled.SimCard
    )

    data object Health : ExpenseCategory(
        categoryId = 9,
        categoryName = "Health",
        icon = Icons.Filled.MedicalServices
    )

    data object Cash : ExpenseCategory(
        categoryId = 10,
        categoryName = "Cash",
        icon = Icons.Filled.LocalAtm
    )

    data object Other : ExpenseCategory(
        categoryId = 100,
        categoryName = "Other",
        icon = Icons.Filled.MoreHoriz
    )

    companion object {
        val all: List<ExpenseCategory> = listOf(
            Food,
            Cafe,
            Groceries,
            Transport,
            Sightseeing,
            Shopping,
            Hotel,
            Sim,
            Health,
            Cash,
            Other
        )

        fun fromId(categoryId: Int): ExpenseCategory =
            all.find { it.categoryId == categoryId } ?: Other
    }
}
