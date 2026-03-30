package com.disetouchi.tripexpense.data.room

import androidx.room.TypeConverter

/**
 * Minimal converter for List<String>.
 * Currency codes are fixed-length and don't contain commas, so CSV is safe here.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        value.joinToString(separator = ",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")
}