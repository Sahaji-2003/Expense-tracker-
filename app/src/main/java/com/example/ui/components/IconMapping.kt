package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapping {
    val iconsMap = mapOf(
        "Home" to Icons.Default.Home,
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "Check" to Icons.Default.Check,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "PlayArrow" to Icons.Default.PlayArrow,
        "Info" to Icons.Default.Info,
        "Edit" to Icons.Default.Edit,
        "Add" to Icons.Default.Add,
        "Warning" to Icons.Default.Warning,
        "Delete" to Icons.Default.Delete,
        "Settings" to Icons.Default.Settings,
        "Refresh" to Icons.Default.Refresh
    )

    fun getIcon(name: String): ImageVector {
        return iconsMap[name] ?: Icons.Default.Add
    }
}
