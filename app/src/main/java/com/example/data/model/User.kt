package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val name: String,
    val pin: String,
    val monthlySalary: Double = 0.0
)
