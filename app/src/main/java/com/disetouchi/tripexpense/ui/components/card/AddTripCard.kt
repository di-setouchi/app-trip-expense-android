package com.disetouchi.tripexpense.ui.components.card

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.disetouchi.tripexpense.R

@Composable
internal fun AddTripCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(24.dp)
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                // dashed rounded rect border (subtle, rounded cap)
                val strokeWidth = 1.5.dp.toPx()
                val dash = 6.dp.toPx()
                val gap = 6.dp.toPx()

                val paint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                    color = outline.toArgb()
                    strokeCap = Paint.Cap.ROUND
                    pathEffect = DashPathEffect(floatArrayOf(dash, gap), 0f)
                }

                val inset = strokeWidth / 2
                val rect = RectF(
                    inset,
                    inset,
                    size.width - inset,
                    size.height - inset
                )

                val radius = 24.dp.toPx()
                drawContext.canvas.nativeCanvas.drawRoundRect(rect, radius, radius, paint)
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, outline)
            ) {
                Text(
                    text = stringResource(R.string.home_add_button),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.home_add_trip_title),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}