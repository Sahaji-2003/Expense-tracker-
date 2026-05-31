package com.example.data.model

data class RecurringExpenseWithCategory(
    val id: Long,
    val name: String,
    val amount: Double,
    val categoryId: Long,
    val dayOfMonth: Int,
    val isAutoDeduct: Boolean,
    val lastProcessedMonth: String,
    val categoryName: String,
    val categoryIconName: String,
    val categoryColorHex: String
)
