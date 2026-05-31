package com.example.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtil {
    fun formatCurrency(amount: Double): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            formatter.format(amount)
        } catch (e: Exception) {
            try {
                "₹" + String.format(Locale.US, "%,.2f", amount)
            } catch (e2: Exception) {
                "₹" + amount.toString()
            }
        }
    }
}
