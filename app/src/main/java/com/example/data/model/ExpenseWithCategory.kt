package com.example.data.model

data class ExpenseWithCategory(
    val id: Long,
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val categoryId: Long,
    val paymentMode: String,
    val categoryName: String,
    val categoryIconName: String,
    val categoryColorHex: String
)
