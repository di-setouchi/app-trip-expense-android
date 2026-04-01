package com.disetouchi.tripexpense.ui.screen.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.disetouchi.tripexpense.R
import com.disetouchi.tripexpense.data.repository.RepositoryProvider
import com.disetouchi.tripexpense.ui.components.rememberSingleTapGuard
import com.disetouchi.tripexpense.ui.components.section.expenseSection
import com.disetouchi.tripexpense.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onBackClick: () -> Unit = {},
    onEditTripClick: (tripId: Long) -> Unit = {},
    onDeleteTripClick: (tripId: Long) -> Unit = {},
    onExpenseDetailClick: (tripId: Long, expenseId: Long) -> Unit,
) {
    val viewModel: TripDetailViewModel = viewModel(
        factory = TripDetailViewModelFactory(
            tripId = tripId,
            tripRepository = RepositoryProvider.tripRepository,
            expenseRepository = RepositoryProvider.expenseRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trip = uiState.trip

    var showDeleteFailedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TripDetailEvent.Deleted -> onDeleteTripClick(tripId)
                is TripDetailEvent.Error -> {
                    showDeleteFailedDialog = true
                }
            }
        }
    }

    val tripTitle = trip?.tripName ?: "—"
    val baseCurrency = trip?.baseCurrencyCode ?: "—"

    val duration = if (trip != null) {
        DateTimeUtil.formatDuration(trip.startDate, trip.endDate)
    } else {
        "—"
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // state of buttons
    val backGuard = rememberSingleTapGuard()
    val menuActionGuard = rememberSingleTapGuard()

    Scaffold(
        // Trip Title(Top Bar)
        topBar = {
            TripDetailTopBar(
                title = tripTitle,
                menuExpanded = menuExpanded,
                onMenuOpen = { menuExpanded = true },
                onMenuDismiss = { menuExpanded = false },
                backLocked = backGuard.isLocked,
                menuActionLocked = menuActionGuard.isLocked,
                onBackClick = { backGuard.runIfUnlocked { onBackClick() } },
                onEditClick = {
                    menuExpanded = false
                    menuActionGuard.runIfUnlocked { onEditTripClick(tripId) }
                },
                onDeleteClick = {
                    menuExpanded = false
                    showDeleteDialog = true
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Trip overview section
                TripOverviewCard(
                    duration = duration,
                    baseCurrency = baseCurrency
                )
            }
            item {
                // Summary section
                SummarySection(totalsByCurrency = uiState.totalsByCurrency)
            }

            // Expenses section
            if (trip != null){
                expenseSection(
                    expenseList = uiState.expenses,
                    onExpenseCardClick = { tripId, expenseId -> onExpenseDetailClick(tripId, expenseId) }
                )
            }
        }

        // Delete dialog
        if (showDeleteDialog) {
            DeleteTripDialog(
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.deleteTrip()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        // Delete failed dialog
        if (showDeleteFailedDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteFailedDialog = false },
                title = { Text(stringResource(R.string.delete_trip_failed_dialog_title)) },
                text = {
                    Text(stringResource(R.string.delete_trip_failed_dialog_message))
                },
                confirmButton = {
                    TextButton(onClick = { showDeleteFailedDialog = false }) {
                        Text(stringResource(R.string.ok_button))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDetailTopBar(
    title: String,
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
        title = { Text(text = title) },
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
        actions = {
            IconButton(
                onClick = onMenuOpen,
                enabled = !menuActionLocked
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More"
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onMenuDismiss,
//                modifier = Modifier.widthIn(max = 160.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_button)) },
//                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    onClick = onEditClick,
                    enabled = !menuActionLocked
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.delete_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
//                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    onClick = onDeleteClick,
                    enabled = !menuActionLocked
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun TripOverviewCard(
    duration: String,
    baseCurrency: String,
) {
    Text(
        text = stringResource(R.string.trip_detail_trip_overview_title),
        style = MaterialTheme.typography.headlineSmall
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.trip_detail_trip_overview_base_currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = baseCurrency,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    totalsByCurrency: List<Pair<String, String>>,
) {
    Text(
        text = stringResource(R.string.trip_detail_summary_title),
        style = MaterialTheme.typography.headlineSmall
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            totalsByCurrency.forEach { (currency, totalText) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.trip_detail_total_with_currency, currency),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteTripDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.trip_detail_delete_dialog_title)) },
        text = { Text(stringResource(R.string.trip_detail_delete_dialog_message)) },
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