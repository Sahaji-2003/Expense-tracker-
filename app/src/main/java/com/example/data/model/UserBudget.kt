package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_budgets")
data class UserBudget(
    @PrimaryKey val userId: String,
    val monthlySalary: Double = 0.0
)
