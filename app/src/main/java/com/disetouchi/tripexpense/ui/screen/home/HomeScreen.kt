package com.disetouchi.tripexpense.ui.screen.home

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import com.disetouchi.tripexpense.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.disetouchi.tripexpense.data.repository.RepositoryProvider
import com.disetouchi.tripexpense.ui.components.section.expenseSection
import com.disetouchi.tripexpense.domain.model.Trip
import com.disetouchi.tripexpense.ui.components.card.AddTripCard
import com.disetouchi.tripexpense.ui.components.card.NoTripCard
import com.disetouchi.tripexpense.ui.components.card.TripCard
import com.disetouchi.tripexpense.util.DateTimeUtil
import kotlinx.coroutines.flow.drop
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTripClick: (tripId: Long) -> Unit = {},
    onAddTripClick: () -> Unit = {},
    onAddExpenseClick: (tripId: Long) -> Unit = {},
    onExpenseCardClick: (tripId: Long, expenseId: Long) -> Unit,
    onSyncClick: () -> Unit = {},
) {
    // Explicitly use Activity as ViewModelStoreOwner to share the HomeViewModel instance with AppNav.
    // This prevents double instantiation and ensures UI state consistency.
    val viewModel: HomeViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        factory = HomeViewModelFactory(
            tripRepository = RepositoryProvider.tripRepository,
            expenseRepository = RepositoryProvider.expenseRepository,
            rateSnapshotRepository = RepositoryProvider.rateSnapshotRepository,
            userPreferencesRepository = RepositoryProvider.userPreferencesRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val hasTrip = uiState.trips.isNotEmpty()
    val selectedTripId = uiState.selectedTripId

    var showRateFetchFailedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.RateFetchFailed -> {
                    showRateFetchFailedDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title =
                    {},
                actions = {
                    Column(
                        modifier = Modifier.padding(end = 4.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val dateText = if (uiState.lastSyncTimestamp > 0) {
                            val instant = Instant.ofEpochMilli(uiState.lastSyncTimestamp)
                            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.ENGLISH)
                            instant.atZone(ZoneId.systemDefault()).format(formatter)
                        } else {
                            "-"
                        }
                        Text(
                            text = stringResource(R.string.exchange_rate_last_updated),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SyncButton(
                        isSyncing = uiState.isSyncing,
                        onClick = { 
                            viewModel.onSyncClick()
                            onSyncClick() // keep the original callback if needed by parent
                        }
                    )
                }
            )
        },
        // If there is more than one trip, show FAB(+) on bottom right to add Expenses section
        floatingActionButton = {
            if (hasTrip && selectedTripId != null) {
                FloatingActionButton(
                    onClick = {
                        onAddExpenseClick(selectedTripId)
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Expense")
                }
            }
        },
        // for Edge to Edge
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
                // Trip section
                TripSection(
                    tripList = uiState.trips,
                    selectedTripId = selectedTripId,
                    onTripClick = onTripClick,
                    onAddTripClick = onAddTripClick,
                    onTripSelected = viewModel::onTripSelected
                )
            }
            // Expenses section
            if (hasTrip && selectedTripId != null) {
                expenseSection(
                    expenseList = uiState.expenses,
                    onExpenseCardClick = onExpenseCardClick
                )
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
    }
}

@Composable
private fun SyncButton(
    isSyncing: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SyncButtonTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncButtonRotation"
    )

    IconButton(
        onClick = onClick,
        enabled = !isSyncing
    ) {
        Icon(
            imageVector = Icons.Filled.Sync,
            contentDescription = "Sync",
            modifier = Modifier.rotate(if (isSyncing) angle else 0f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TripSection(
    tripList: List<Trip>,
    selectedTripId: Long?,
    onTripClick: (tripId: Long) -> Unit,
    onAddTripClick: () -> Unit,
    onTripSelected: (tripId: Long) -> Unit,
) {
    Text(
        text = stringResource(R.string.home_trip_area_title),
        style = MaterialTheme.typography.headlineSmall
    )
    val hasTrip = tripList.isNotEmpty()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val pageWidth = screenWidthDp * 0.65f

    val pageCount = if (!hasTrip) 2 else (tripList.size + 1)
    val pagerState = rememberPagerState(initialPage = 0) { pageCount }

    val currentTripList by rememberUpdatedState(tripList)
    val currentSelectedTripId by rememberUpdatedState(selectedTripId)

    // Sync Pager state with selectedTripId from ViewModel (Source of Truth)
    LaunchedEffect(selectedTripId, tripList) {
        if (selectedTripId == null || tripList.isEmpty()) return@LaunchedEffect
        
        val targetIndex = tripList.indexOfFirst { it.tripId == selectedTripId }
        if (targetIndex != -1 && pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    // Update ViewModel when Pager is swiped.
    // Use .drop(1) to prevent accidental state overwrite on initial load.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .drop(1)
            .collect { page ->
                if (currentTripList.isNotEmpty() && page in currentTripList.indices) {
                    val swipedTripId = currentTripList[page].tripId
                    if (swipedTripId != currentSelectedTripId) {
                        onTripSelected(swipedTripId)
                    }
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(pageWidth),
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        when {
            // hasTrip=false: 0=NoTrip, 1=AddTrip
            !hasTrip && page == 0 -> {
                NoTripCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2.2f)
                )
            }
            // hasTrip=true & not last page: TripCard
            hasTrip && page < tripList.size -> {
                val trip = tripList[page]
                TripCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2.2f),
                    title = trip.tripName,
                    durationText = DateTimeUtil.formatDuration(trip.startDate, trip.endDate),
                    onClick = { onTripClick(trip.tripId) }
                )
            }
            // hasTrip=true & last page: AddTrip
            else -> {
                AddTripCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2.2f),
                    onClick = onAddTripClick
                )
            }
        }
    }
}
