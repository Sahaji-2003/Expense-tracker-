package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_budgets")
data class UserBudget(
    @PrimaryKey val id: Int = 1, // Singleton row
    val monthlySalary: Double = 50000.0
)
