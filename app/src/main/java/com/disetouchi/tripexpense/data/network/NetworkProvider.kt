package com.disetouchi.tripexpense.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Singleton object to provide network-related instances such as Moshi and Retrofit API services.
 */
object NetworkProvider {
    // Define the base URL for the API
    private const val BASE_URL = "https://api.frankfurter.dev/"

    // Initialize Moshi to parse JSON into Kotlin data classes
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Initialize Retrofit with the base URL and Moshi converter
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Create the implementation of the FrankfurterApiService interface lazily
    val frankfurterApi: FrankfurterApiService by lazy {
        retrofit.create(FrankfurterApiService::class.java)
    }
}