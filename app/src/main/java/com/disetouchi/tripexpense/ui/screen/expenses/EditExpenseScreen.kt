package com.disetouchi.tripexpense.ui.screen.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.disetouchi.tripexpense.R
import com.disetouchi.tripexpense.data.repository.RepositoryProvider
import com.disetouchi.tripexpense.model.*
import com.disetouchi.tripexpense.ui.components.CalendarDialog
import com.disetouchi.tripexpense.ui.components.SaveButton
import com.disetouchi.tripexpense.ui.components.hideKeyboard
import com.disetouchi.tripexpense.ui.components.rememberSingleTapGuard
import com.disetouchi.tripexpense.util.DateTimeUtil
import java.time.LocalDate

/**
 * ViewModel-driven EditExpense screen.
 * - expenseId == null : Add expense
 * - expenseId != null : Edit expense
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    tripId: Long,
    expenseId: Long? = null,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    val viewModel: EditExpenseViewModel = viewModel(
        factory = EditExpenseViewModelFactory(
            tripId = tripId,
            expenseId = expenseId,
            tripRepository = RepositoryProvider.tripRepository,
            expenseRepository = RepositoryProvider.expenseRepository,
            rateSnapshotRepository = RepositoryProvider.rateSnapshotRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var showRateFetchFailedDialog by remember { mutableStateOf(false) }
    var showSaveFailedDialog by remember { mutableStateOf(false) }

    val backGuard = rememberSingleTapGuard()
    val saveGuard = rememberSingleTapGuard()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditExpenseEvent.Saved -> onSaveClick()
                EditExpenseEvent.RateUnavailable -> {
                    showRateFetchFailedDialog = true
                    // Unlock the save button to allow the user to try again
                    saveGuard.unlock()
                }
                EditExpenseEvent.SaveFailed -> {
                    showSaveFailedDialog = true
                    // Unlock the save button to allow the user to try again
                    saveGuard.unlock()
                }
            }
        }
    }

    val title =
        if (uiState.isEdit) stringResource(R.string.edit_expense_title) else stringResource(R.string.add_expense_title)

    Scaffold(
        topBar = {
            EditExpenseTopBar(
                title = title,
                backLocked = backGuard.isLocked,
                onBackClick = { backGuard.runIfUnlocked { onBackClick() } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .hideKeyboard(focusManager)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Local Currency Section
                LocalCurrencyAmountInput(
                    currencyCode = uiState.localCurrencyCode,
                    amount = uiState.localAmountText,
                    currencyCodes = uiState.localCurrencyCodes,
                    onAmountChange = viewModel::onLocalAmountChange,
                    onCurrencyChange = viewModel::onLocalCurrencyChange
                )

                // Base Currency Section (Read-only)
                BaseCurrencyAmountDisplay(
                    currencyCode = uiState.baseCurrencyCode,
                    amount = uiState.baseAmountText
                )

                // Exchange rate section
                ExchangeRateRow(
                    label = stringResource(R.string.edit_expense_exchange_rate_label),
                    valueText = uiState.exchangeRateText
                )

                // Date Section
                DatePickerRow(
                    label = stringResource(R.string.edit_expense_date_label),
                    date = uiState.occurredAt,
                    onDateSelected = viewModel::onDateChange
                )

                // Category Section
                CategoryPickerRow(
                    label = stringResource(R.string.edit_expense_category_label),
                    selectedCategoryId = uiState.categoryId,
                    categories = ExpenseCategory.all,
                    onCategorySelected = { viewModel.onCategoryChange(it) }
                )

                // Note Section
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = { Text(stringResource(R.string.edit_expense_note_label)) },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            // Save Button
            // We use Box wrapper for a potential CircularProgressIndicator layer if needed later,
            // matching EditTripScreen's structure for consistency.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                SaveButton(
                    enabled = uiState.canSave && !saveGuard.isLocked,
                    onSaveClick = { saveGuard.runIfUnlocked { viewModel.onSaveClick() } }
                )
            }
        }
    }
    if (showRateFetchFailedDialog) {
        AlertDialog(
            onDismissRequest = { showRateFetchFailedDialog = false },
            title = { Text(stringResource(R.string.rate_fetch_failed_dialog_title)) },
            text = {
                Text(stringResource(R.string.rate_fetch_failed_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = { showRateFetchFailedDialog = false }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }
    if (showSaveFailedDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFailedDialog = false },
            title = { Text(stringResource(R.string.save_expense_failed_dialog_title)) },
            text = {
                Text(stringResource(R.string.save_expense_failed_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = { showSaveFailedDialog = false }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseTopBar(
    title: String,
    backLocked: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                enabled = !backLocked
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalCurrencyAmountInput(
    currencyCode: String,
    amount: String?,
    currencyCodes: List<String>,
    onAmountChange: (localAmountText: String) -> Unit,
    onCurrencyChange: (localCurrencyCode: String) -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalCurrencySelector(
            currencyCode = currencyCode,
            options = currencyCodes,
            textColor = textColor,
            onCurrencyChange = onCurrencyChange,
            modifier = Modifier.width(120.dp)
        )

        AmountField(
            amount = amount ?: "",
            isEditable = true,
            textColor = textColor,
            onAmountChange = onAmountChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalCurrencySelector(
    currencyCode: String,
    options: List<String>,
    textColor: Color,
    onCurrencyChange: (localCurrencyCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText = remember(currencyCode) {
        val info = CurrencyForFrankfurterApi.fromCode(currencyCode)
        val flag = info?.nationalFlagEmoji.orEmpty()
        if (flag.isBlank()) currencyCode else "$flag $currencyCode"
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
                color = textColor
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { code ->
                val info = CurrencyForFrankfurterApi.fromCode(code)
                val flag = info?.nationalFlagEmoji.orEmpty()

                DropdownMenuItem(
                    text = { Text(if (flag.isBlank()) code else "$flag $code") },
                    onClick = {
                        onCurrencyChange(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BaseCurrencyAmountDisplay(
    currencyCode: String,
    amount: String,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BaseCurrencyLabel(
            currencyCode = currencyCode,
            textColor = textColor,
            modifier = Modifier.width(120.dp)
        )

        Text(
            text = amount,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
                color = textColor,
                textAlign = TextAlign.End
            )
        )
    }
}

@Composable
private fun BaseCurrencyLabel(
    currencyCode: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val displayText = remember(currencyCode) {
        val info = CurrencyForFrankfurterApi.fromCode(currencyCode)
        val flag = info?.nationalFlagEmoji.orEmpty()
        if (flag.isBlank()) currencyCode else "$flag $currencyCode"
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = 24.sp,
            color = textColor
        ),
        modifier = modifier
            .fillMaxHeight()
            .wrapContentHeight(Alignment.CenterVertically)
    )
}

@Composable
private fun AmountField(
    amount: String,
    isEditable: Boolean,
    textColor: Color,
    onAmountChange: (localAmountText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = MaterialTheme.typography.headlineSmall.copy(
        textAlign = TextAlign.End,
        fontSize = 24.sp,
        color = textColor
    )

    if (isEditable) {
        BasicTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = modifier,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (amount.isEmpty()) {
                        Text(
                            "0.00",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp,
                                color = textColor
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    } else {
        Text(
            text = amount,
            modifier = modifier,
            style = textStyle,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExchangeRateRow(
    label: String,
    valueText: String,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerRow(
    label: String,
    date: LocalDate,
    onDateSelected: (selectedDate: LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(DateTimeUtil.format(date), style = MaterialTheme.typography.bodyLarge)
    }

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                .toEpochMilli()
        )
        CalendarDialog(
            state = state,
            onDismissRequest = { showDialog = false },
            onConfirmClick = { millis ->
                val selected = java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                onDateSelected(selected)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerRow(
    label: String,
    selectedCategoryId: Int,
    categories: List<ExpenseCategory>,
    onCategorySelected: (selectedCategoryId: Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = remember(selectedCategoryId) { ExpenseCategory.fromId(selectedCategoryId) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.wrapContentWidth()
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = selected.icon,
                    contentDescription = selected.categoryName,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selected.categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 180.dp)
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.categoryName,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.categoryName)
                            }
                        },
                        onClick = {
                            onCategorySelected(item.categoryId)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}