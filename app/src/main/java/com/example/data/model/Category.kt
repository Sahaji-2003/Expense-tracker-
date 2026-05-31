package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String, // Mapping string for icons
    val colorHex: String, // Color representation in hex, e.g. "#4CAF50"
    val monthlyLimit: Double
)
