package com.disetouchi.tripexpense.ui.screen.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.disetouchi.tripexpense.R
import com.disetouchi.tripexpense.data.repository.RepositoryProvider
import com.disetouchi.tripexpense.model.ExpenseCategory
import com.disetouchi.tripexpense.ui.components.rememberSingleTapGuard
import com.disetouchi.tripexpense.util.AmountUtil
import com.disetouchi.tripexpense.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    tripId: Long,
    onBackClick: () -> Unit = {},
    onEditExpenseClick: (expenseId: Long) -> Unit = {},
    onDeleteExpenseClick: (expenseId: Long) -> Unit = {},
) {
    val viewModel: ExpenseDetailViewModel = viewModel(
        factory = ExpenseDetailViewModelFactory(
            tripId = tripId,
            expenseId = expenseId,
            expenseRepository = RepositoryProvider.expenseRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val expenseData = uiState.expense

    var showDeleteFailedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ExpenseDetailEvent.Deleted -> onDeleteExpenseClick(expenseId)
                is ExpenseDetailEvent.Error -> {
                    showDeleteFailedDialog = true
                }
            }
        }
    }

    val localAmountText = remember(expenseData?.localAmountMinor, expenseData?.localCurrencyCode) {
        if (expenseData != null) {
            AmountUtil.formatMinorAmountWithSymbol(expenseData.localAmountMinor, expenseData.localCurrencyCode)
        } else ""
    }

    val baseAmountText = remember(expenseData?.baseAmountMinor, expenseData?.baseCurrencyCode) {
        if (expenseData != null) {
            AmountUtil.formatMinorAmountWithSymbol(expenseData.baseAmountMinor, expenseData.baseCurrencyCode)
        } else null
    }

    val categoryId = remember(expenseData?.categoryId) {
        expenseData?.categoryId ?: ExpenseCategory.Other.categoryId
    }

    val date = DateTimeUtil.format(expenseData?.occurredAt)

    val exchangeRate = remember(expenseData?.rateUsedMicros) {
        val micros = expenseData?.rateUsedMicros ?: return@remember ""
        AmountUtil.formatRateMicros(micros)
    }

    val rateFetchedAt = DateTimeUtil.format(expenseData?.rateFetchedAt)

    val note = expenseData?.note ?: ""

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // state of buttons
    val backGuard = rememberSingleTapGuard()
    val menuActionGuard = rememberSingleTapGuard()

    Scaffold(
        topBar = {
            ExpenseDetailTopBar(
                menuExpanded = menuExpanded,
                onMenuOpen = { menuExpanded = true },
                onMenuDismiss = { menuExpanded = false },
                backLocked = backGuard.isLocked,
                menuActionLocked = menuActionGuard.isLocked,
                onBackClick = { backGuard.runIfUnlocked { onBackClick() } },
                onEditClick = {
                    menuExpanded = false
                    menuActionGuard.runIfUnlocked { onEditExpenseClick(expenseId) }
                },
                onDeleteClick = {
                    menuExpanded = false
                    menuActionGuard.runIfUnlocked { showDeleteDialog = true }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = localAmountText,
                    style = MaterialTheme.typography.headlineLarge,
                )
                baseAmountText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            CategoryDetailItem(
                label = stringResource(R.string.expense_detail_category),
                categoryId = categoryId
            )
            DetailItem(
                label = stringResource(R.string.expense_detail_date),
                value = date
            )
            DetailItem(
                label = stringResource(R.string.expense_detail_exchange_rate),
                value = exchangeRate
            )
            DetailItem(
                label = stringResource(R.string.expense_detail_rate_fetched),
                value = rateFetchedAt
            )

            if (note.isNotBlank()) {
                NoteSection(note = note)
            }
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        DeleteExpenseDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteExpense()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Delete failed dialog
    if (showDeleteFailedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteFailedDialog = false },
            title = { Text(stringResource(R.string.delete_expense_failed_dialog_title)) },
            text = {
                Text(stringResource(R.string.delete_expense_failed_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = { showDeleteFailedDialog = false }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetailTopBar(
    menuExpanded: Boolean,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    backLocked: Boolean,
    menuActionLocked: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                enabled = !backLocked
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onMenuOpen) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onMenuDismiss
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_button)) },
                    onClick = onEditClick,
                    enabled = !menuActionLocked
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.delete_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = onDeleteClick,
                    enabled = !menuActionLocked
                )
            }
        }
    )
}

@Composable
private fun DeleteExpenseDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.expense_detail_dialog_title)) },
        text = { Text(stringResource(R.string.expense_detail_delete_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete_button),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CategoryDetailItem(
    label: String,
    categoryId: Int
) {
    val category = remember(categoryId) { ExpenseCategory.fromId(categoryId) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.categoryName,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun NoteSection(note: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.expense_detail_note_section),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}