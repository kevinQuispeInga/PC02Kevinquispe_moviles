package com.example.uesanapp.data.remote

import com.example.uesanapp.data.model.ConversionRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirestoreManager {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveRates(ratesMap: Map<String, Double>): Result<Unit> {
        return try {
            for ((code, rate) in ratesMap) {
                val rateData = hashMapOf(
                    "code" to code,
                    "rate" to rate,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                firestore.collection("rates").document(code).set(rateData).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveConversion(
        userId: String,
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        result: Double
    ): Result<Unit> {
        return try {
            val conversionData = hashMapOf(
                "userId" to userId,
                "amount" to amount,
                "fromCurrency" to fromCurrency,
                "toCurrency" to toCurrency,
                "result" to result,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            firestore.collection("conversions").add(conversionData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserConversions(userId: String): Result<List<ConversionRecord>> {
        return try {
            val querySnapshot = firestore.collection("conversions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val conversions = querySnapshot.documents.mapNotNull { doc ->
                val record = doc.toObject(ConversionRecord::class.java)
                record?.copy(id = doc.id)
            }.sortedWith(compareByDescending { it.timestamp?.seconds ?: 0L })
            Result.success(conversions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
