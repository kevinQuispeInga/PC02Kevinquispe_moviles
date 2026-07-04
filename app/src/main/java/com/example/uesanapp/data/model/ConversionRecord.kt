package com.example.uesanapp.data.model

import com.google.firebase.Timestamp

data class ConversionRecord(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val fromCurrency: String = "",
    val toCurrency: String = "",
    val result: Double = 0.0,
    val timestamp: Timestamp? = null
)
