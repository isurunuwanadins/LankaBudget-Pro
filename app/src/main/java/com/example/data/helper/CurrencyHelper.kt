package com.example.data.helper

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyHelper {
    var activeCurrencyCode: String = "LKR"

    fun getExchangeRate(currencyCode: String): Double {
        return when (currencyCode) {
            "USD" -> 1.0 / 300.0
            "EUR" -> 1.0 / 325.0
            "GBP" -> 1.0 / 380.0
            "INR" -> 1.0 / 3.6
            else -> 1.0
        }
    }

    fun format(amount: Double, currencyCode: String = activeCurrencyCode): String {
        val rate = getExchangeRate(currencyCode)
        val converted = amount * rate

        return try {
            val currencyInstance = Currency.getInstance(currencyCode)
            val symbol = currencyInstance.getSymbol(Locale.getDefault())
            
            val numFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = if (converted % 1.0 == 0.0) 0 else 2
                maximumFractionDigits = 2
            }
            val formattedNumber = numFormat.format(converted)
            
            if (currencyCode == "LKR" || currencyCode == "INR") {
                "$formattedNumber $symbol"
            } else {
                "$symbol$formattedNumber"
            }
        } catch (e: Exception) {
            val symbol = when (currencyCode) {
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "INR" -> "₹"
                else -> "LKR"
            }
            if (symbol == "LKR" || symbol == "INR") {
                if (converted % 1.0 == 0.0) {
                    String.format("%,.0f %s", converted, symbol)
                } else {
                    String.format("%,.2f %s", converted, symbol)
                }
            } else {
                if (converted % 1.0 == 0.0) {
                    String.format("%s%,.0f", symbol, converted)
                } else {
                    String.format("%s%,.2f", symbol, converted)
                }
            }
        }
    }

    fun parse(amountString: String): Double? {
        val clean = amountString.replace("[^\\d.]".toRegex(), "")
        val parsed = clean.toDoubleOrNull() ?: return null
        
        val rate = getExchangeRate(activeCurrencyCode)
        return if (rate > 0) parsed / rate else parsed
    }
}
