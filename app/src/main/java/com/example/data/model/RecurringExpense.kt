package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val categoryId: Long,
    val dayOfMonth: Int, // Day of the month to execute (e.g. 1 to 28/31)
    val isAutoDeduct: Boolean = false,
    val lastProcessedMonth: String = "", // Tracks "YYYY-MM" to prevent duplicate logging
    val userId: String = ""
)
