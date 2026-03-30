package com.disetouchi.tripexpense.ui.screen.trip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.disetouchi.tripexpense.R
import com.disetouchi.tripexpense.data.repository.RepositoryProvider
import com.disetouchi.tripexpense.model.CurrencyForFrankfurterApi
import com.disetouchi.tripexpense.ui.components.CalendarDialog
import com.disetouchi.tripexpense.ui.components.SaveButton
import com.disetouchi.tripexpense.ui.components.hideKeyboard
import com.disetouchi.tripexpense.ui.components.rememberSingleTapGuard
import com.disetouchi.tripexpense.util.DateTimeUtil
import java.time.LocalDate

/**
 * ViewModel-driven EditTrip screen.
 * - tripId == null : Add trip
 * - tripId != null : Edit trip (base currency disabled)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripScreen(
    tripId: Long? = null,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    val viewModel: EditTripViewModel = viewModel(
        factory = EditTripViewModelFactory(
            tripId = tripId,
            tripRepository = RepositoryProvider.tripRepository,
            rateSnapshotRepository = RepositoryProvider.rateSnapshotRepository,
            userPreferencesRepository = RepositoryProvider.userPreferencesRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    val backGuard = rememberSingleTapGuard()
    val saveGuard = rememberSingleTapGuard()

    var showRateFetchFailedDialog by remember { mutableStateOf(false) }
    var showSaveFailedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditTripEvent.Saved -> onSaveClick()
                EditTripEvent.RateFetchFailed -> {
                    showRateFetchFailedDialog = true
                    // Unlock the save button to allow the user to try again after a network error
                    saveGuard.unlock()
                }
                EditTripEvent.SaveFailed -> {
                    showSaveFailedDialog = true
                    // Unlock the save button to allow the user to try again after a save failure
                    saveGuard.unlock()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            EditTripTopBar(
                title = if (uiState.isEdit) stringResource(R.string.edit_trip_title) else stringResource(R.string.add_trip_title),
                backLocked = backGuard.isLocked,
                onBackClick = { backGuard.runIfUnlocked { onBackClick() } }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .hideKeyboard(focusManager)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    TripNameField(
                        value = uiState.tripName,
                        onValueChange = viewModel::onTripNameChange
                    )

                    BaseCurrencyField(
                        value = uiState.baseCurrencyCode,
                        onValueChange = viewModel::onBaseCurrencyChange,
                        enabled = !uiState.isEdit
                    )

                    LocalCurrenciesField(
                        baseCurrency = uiState.baseCurrencyCode,
                        selected = uiState.localCurrencyCodes,
                        enabled = true,
                        onAddCurrency = viewModel::addLocalCurrency,
                        onRemoveCurrency = viewModel::removeLocalCurrency
                    )

                    DateRangeFields(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        onStartDateChange = viewModel::onStartDateChange,
                        onEndDateChange = viewModel::onEndDateChange
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    SaveButton(
                        enabled = !uiState.isSaving && !saveGuard.isLocked && uiState.isInputValid,
                        onSaveClick = {
                            saveGuard.runIfUnlocked { viewModel.onSaveClick() }
                        }
                    )
                }
            }

            // Show indicator and overlay during saving/API calling process
            if (uiState.isSaving) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        if (showRateFetchFailedDialog) {
            AlertDialog(
                onDismissRequest = { showRateFetchFailedDialog = false },
                title = { Text(stringResource(R.string.rate_fetch_failed_dialog_title)) },
                text = { Text(stringResource(R.string.rate_fetch_failed_dialog_message)) },
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
                title = { Text(stringResource(R.string.save_trip_failed_dialog_title)) },
                text = { Text(stringResource(R.string.save_trip_failed_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { showSaveFailedDialog = false }) {
                        Text(stringResource(R.string.ok_button))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTripTopBar(
    title: String,
    backLocked: Boolean,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                enabled = !backLocked
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun TripNameField(
    value: String,
    onValueChange: (tripName: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.edit_trip_trip_name),
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseCurrencyField(
    value: String,
    onValueChange: (baseCurrencyCode: String) -> Unit,
    enabled: Boolean,
) {
    val currencyInfo = remember { CurrencyForFrankfurterApi.all }

    val currencyDisplayValue = remember(value) {
        val currencyInfo = CurrencyForFrankfurterApi.fromCode(value)
        val flag = currencyInfo?.nationalFlagEmoji.orEmpty()
        if (flag.isBlank() || value.isBlank()) value else "$flag $value"
    }

    var expanded by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.edit_trip_base_currency),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = { showInfo = !showInfo },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Show info",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(visible = showInfo) {
            Text(
                text = stringResource(R.string.edit_trip_base_currency_info_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currencyDisplayValue,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                enabled = enabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencyInfo.forEach { currency ->
                    val code = currency.currencyCode
                    val flag = currency.nationalFlagEmoji

                    DropdownMenuItem(
                        text = { Text("$flag $code") },
                        onClick = {
                            onValueChange(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun LocalCurrenciesField(
    baseCurrency: String,
    selected: List<String>,
    enabled: Boolean,
    onAddCurrency: (localCurrencyCode: String) -> Unit,
    onRemoveCurrency: (localCurrencyCode: String) -> Unit,
) {
    val currencyOptions = remember { CurrencyForFrankfurterApi.all }

    var expanded by remember { mutableStateOf(false) }

    val available = remember(baseCurrency, selected) {
        currencyOptions
            .map { it.currencyCode }
            .filter { code -> code != baseCurrency && !selected.contains(code) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.edit_trip_local_currencies),
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selected.forEach { code ->
                AssistChip(
                    onClick = {},
                    enabled = enabled,
                    label = { Text(code) },
                    trailingIcon = {
                        IconButton(
                            onClick = { if (enabled) onRemoveCurrency(code) },
                            enabled = enabled,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove"
                            )
                        }
                    }
                )
            }

        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled && available.isNotEmpty()) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = stringResource(R.string.edit_trip_local_currencies_drop_down_menu_box),
                onValueChange = {},
                readOnly = true,
                enabled = enabled && available.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                available.forEach { code ->
                    val info = CurrencyForFrankfurterApi.fromCode(code)
                    val flag = info?.nationalFlagEmoji.orEmpty()
                    DropdownMenuItem(
                        text = { Text(if (flag.isBlank()) code else "$flag $code") },
                        onClick = {
                            onAddCurrency(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFields(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateChange: (startDate: LocalDate) -> Unit,
    onEndDateChange: (endDate: LocalDate) -> Unit,
) {
    val isDateError = remember(startDate, endDate) {
        if (startDate != null && endDate != null) {
            startDate.isAfter(endDate)
        } else {
            false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.edit_trip_duration),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateField(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.edit_trip_start_date),
                date = startDate,
                isError = isDateError,
                onDateSelected = onStartDateChange
            )
            DateField(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.edit_trip_end_date),
                date = endDate,
                isError = isDateError,
                onDateSelected = onEndDateChange
            )
        }

        if (isDateError) {
            Text(
                text = stringResource(R.string.edit_trip_duration_warning),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    modifier: Modifier = Modifier,
    label: String,
    date: LocalDate?,
    isError: Boolean = false,
    onDateSelected: (LocalDate) -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    val initialSelectedDateMillis = remember(date) {
        date?.atStartOfDay(java.time.ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

    val state = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)

    Box(modifier = modifier) {
        OutlinedTextField(
            value = DateTimeUtil.format(date),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = isError,
            enabled = true,
            label = { Text(label) }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { open = true }
        )
    }

    if (open) {
        CalendarDialog(
            state = state,
            onDismissRequest = { open = false },
            onConfirmClick = { millis ->
                val selected =
                    java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC)
                        .toLocalDate()
                onDateSelected(selected)
                open = false
            }
        )
    }
}