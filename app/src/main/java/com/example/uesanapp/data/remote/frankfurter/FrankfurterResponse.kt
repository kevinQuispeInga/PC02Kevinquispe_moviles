package com.example.uesanapp.data.remote.frankfurter

data class FrankfurterResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
