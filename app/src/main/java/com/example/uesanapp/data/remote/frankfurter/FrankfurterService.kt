package com.example.uesanapp.data.remote.frankfurter

import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") base: String
    ): FrankfurterResponse
}
