package com.disetouchi.tripexpense.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.disetouchi.tripexpense.domain.model.Expense
import com.disetouchi.tripexpense.model.ExpenseCategory
import com.disetouchi.tripexpense.util.AmountUtil
import com.disetouchi.tripexpense.util.DateTimeUtil

/**
 * A UI-specific representation of an Expense, pre-formatted for display.
 * This ensures the UI layer only handles rendering, not business logic or formatting.
 */
data class ExpenseUiModel(
    val expenseId: Long,
    val tripId: Long,
    val categoryName: String,
    val categoryIcon: ImageVector,
    val dateText: String,
    val localAmountText: String,
    val baseAmountText: String
)

/**
 * Extension function to convert a Domain Expense model to a UI-ready ExpenseUiModel.
 */
fun Expense.toUiModel(): ExpenseUiModel {
    val category = ExpenseCategory.fromId(this.categoryId)
    val formattedLocalAmount = AmountUtil.formatMinorAmountWithSymbol(this.localAmountMinor, this.localCurrencyCode)
    val formattedBaseAmount = AmountUtil.formatMinorAmountWithSymbol(this.baseAmountMinor, this.baseCurrencyCode)
    val formattedDate = DateTimeUtil.format(this.occurredAt)

    return ExpenseUiModel(
        expenseId = this.expenseId,
        tripId = this.tripId,
        categoryName = category.categoryName,
        categoryIcon = category.icon,
        dateText = formattedDate,
        localAmountText = formattedLocalAmount,
        baseAmountText = formattedBaseAmount
    )
}
