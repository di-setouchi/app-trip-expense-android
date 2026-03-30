package com.disetouchi.tripexpense.ui.components.section

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.disetouchi.tripexpense.R
import com.disetouchi.tripexpense.ui.components.card.ExpenseCard
import com.disetouchi.tripexpense.ui.model.ExpenseUiModel

internal fun LazyListScope.expenseSection(
    expenseList: List<ExpenseUiModel>,
    onExpenseCardClick: (tripId: Long, expenseId: Long) -> Unit
) {
    item {
        Text(
            text = stringResource(R.string.home_expenses_area_title),
            style = MaterialTheme.typography.headlineSmall
        )
    }

    items(items = expenseList, key = { it.expenseId }) { expense ->
        ExpenseCard(
            expense = expense,
            onClick = { onExpenseCardClick(expense.tripId, expense.expenseId) }
        )
    }
}
