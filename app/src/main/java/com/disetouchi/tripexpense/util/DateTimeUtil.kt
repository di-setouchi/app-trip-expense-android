package com.disetouchi.tripexpense.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility object for formatting dates and times.
 */
object DateTimeUtil {
    private val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

    /**
     * Formats a LocalDate into "MMM d, yyyy" format.
     */
    fun format(date: LocalDate?): String {
        if (date == null) return ""
        return date.format(formatter)
    }

    /**
     * Formats an Instant into "MMM d, yyyy" format using UTC zone.
     */
    fun format(instant: Instant?): String {
        if (instant == null) return ""
        val date = instant.atZone(ZoneOffset.UTC).toLocalDate()
        return date.format(formatter)
    }

    /**
     * Formats a duration between two dates, e.g., "Oct 1, 2023 – Oct 5, 2023".
     */
    fun formatDuration(startDate: LocalDate?, endDate: LocalDate?): String {
        if (startDate == null || endDate == null) return ""
        return "${format(startDate)} – ${format(endDate)}"
    }
}
