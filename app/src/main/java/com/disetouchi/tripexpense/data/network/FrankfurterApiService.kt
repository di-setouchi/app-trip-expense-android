package com.disetouchi.tripexpense.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApiService {
    /**
     * Call FrankfurterApi
     *
     * @param baseCurrency The base currency code to fetch rates for.
     * It's same to localCurrencyCode.
     */
    @GET("v1/latest")
    suspend fun getLatestRates(
        @Query("base") baseCurrency: String
    ): FrankfurterResponse
}