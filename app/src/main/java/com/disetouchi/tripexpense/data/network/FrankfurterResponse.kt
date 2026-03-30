package com.disetouchi.tripexpense.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FrankfurterResponse(
    @field:Json(name = "base") val base: String,
    @field:Json(name = "date") val date: String,
    @field:Json(name = "rates") val rates: Map<String, Double>
)
