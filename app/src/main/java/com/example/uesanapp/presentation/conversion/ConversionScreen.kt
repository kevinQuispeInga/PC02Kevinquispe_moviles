package com.example.uesanapp.presentation.conversion

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uesanapp.data.remote.FirestoreManager
import com.example.uesanapp.data.remote.frankfurter.FrankfurterRetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var amountStr by remember { mutableStateOf("") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val currencies = listOf("USD", "EUR", "PEN", "GBP", "JPY")

    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    val fallbackRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "PEN" to 3.75,
        "GBP" to 0.79,
        "JPY" to 155.0
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Conversor de Divisas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Tasas actualizadas por Frankfurter API",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Monto Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monto a convertir") },
                    placeholder = { Text("Ej. 100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Currencies Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Currency Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedFrom,
                            onExpandedChange = { expandedFrom = !expandedFrom }
                        ) {
                            OutlinedTextField(
                                value = fromCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("De") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFrom,
                                onDismissRequest = { expandedFrom = false }
                            ) {
                                currencies.filter { it != toCurrency }.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) },
                                        onClick = {
                                            fromCurrency = selection
                                            expandedFrom = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = fromCurrency
                            fromCurrency = toCurrency
                            toCurrency = temp
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Invertir monedas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // To Currency Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedTo,
                            onExpandedChange = { expandedTo = !expandedTo }
                        ) {
                            OutlinedTextField(
                                value = toCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("A") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTo,
                                onDismissRequest = { expandedTo = false }
                            ) {
                                currencies.filter { it != fromCurrency }.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) },
                                        onClick = {
                                            toCurrency = selection
                                            expandedTo = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Convert Button or Loading
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                } else {
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                Toast.makeText(context, "Por favor ingrese un monto válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // 1. Fetch live rates with base USD
                                    val response = try {
                                        FrankfurterRetrofitInstance.api.getLatestRates("USD")
                                    } catch (e: Exception) {
                                        null
                                    }

                                    // 2. Build full rates map (including fixed PEN)
                                    val rates = mutableMapOf<String, Double>()
                                    rates["USD"] = 1.0
                                    rates["PEN"] = 3.75 // pre-defined PEN rate

                                    if (response != null) {
                                        response.rates.forEach { (code, rate) ->
                                            if (code in listOf("EUR", "GBP", "JPY")) {
                                                rates[code] = rate
                                            }
                                        }
                                    } else {
                                        // Fallback to locally predefined rates if API call fails
                                        fallbackRates.forEach { (code, rate) ->
                                            if (code != "USD" && code != "PEN") {
                                                rates[code] = rate
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Sin conexión. Usando tasas predefinidas.", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    // 3. Perform conversion calculation
                                    val rateFrom = rates[fromCurrency] ?: 1.0
                                    val rateTo = rates[toCurrency] ?: 1.0
                                    val convertedAmount = amount * (rateTo / rateFrom)

                                    // 4. Update rates in Firestore
                                    FirestoreManager.saveRates(rates)

                                    // 5. Save conversion log in Firestore
                                    if (userId.isNotEmpty()) {
                                        FirestoreManager.saveConversion(
                                            userId = userId,
                                            amount = amount,
                                            fromCurrency = fromCurrency,
                                            toCurrency = toCurrency,
                                            result = convertedAmount
                                        )
                                    }

                                    withContext(Dispatchers.Main) {
                                        resultText = String.format(
                                            Locale.US,
                                            "%.2f %s equivalen a %.2f %s",
                                            amount, fromCurrency, convertedAmount, toCurrency
                                        )
                                        isLoading = false
                                    }

                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Convertir", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result Display Card
        if (resultText.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resultText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
