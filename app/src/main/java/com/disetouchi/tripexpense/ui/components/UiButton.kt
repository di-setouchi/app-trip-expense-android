package com.disetouchi.tripexpense.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.disetouchi.tripexpense.R

@Composable
fun SaveButton(
    enabled: Boolean,
    onSaveClick: () -> Unit,
) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(stringResource(R.string.save_button))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    state: DatePickerState,
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onConfirmClick(it) } }) {
                Text(stringResource(R.string.ok_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

/**
 * Interface for the single-tap guard.
 */
interface SingleTapGuard {
    val isLocked: Boolean
    fun unlock()
    fun runIfUnlocked(action: () -> Unit)
}

/**
 * Single-tap guard to prevent double-tap actions.
 *
 * - First tap executes and locks.
 * - Subsequent taps are ignored while locked.
 * - Unlocks on Lifecycle.ON_RESUME (useful when navigation fails and the screen stays).
 * - Can be manually unlocked using the [SingleTapGuard.unlock] method.
 */
@Composable
fun rememberSingleTapGuard(): SingleTapGuard {
    val lifecycleOwner = LocalLifecycleOwner.current
    val locked = remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locked.value = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return remember(locked) {
        object : SingleTapGuard {
            override val isLocked: Boolean
                get() = locked.value

            override fun unlock() {
                locked.value = false
            }

            override fun runIfUnlocked(action: () -> Unit) {
                if (!locked.value) {
                    locked.value = true
                    action()
                }
            }
        }
    }
}

fun Modifier.hideKeyboard(focusManager: FocusManager): Modifier = pointerInput(Unit) {
    detectTapGestures(onTap = { focusManager.clearFocus() })
}