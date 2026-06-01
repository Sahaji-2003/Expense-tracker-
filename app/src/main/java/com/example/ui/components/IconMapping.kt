package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapping {
    val iconsMap = mapOf(
        "Home" to Icons.Default.Home,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Favorite" to Icons.Default.Favorite,
        "Star" to Icons.Default.Star,
        "Settings" to Icons.Default.Settings,
        "Info" to Icons.Default.Info,
        "PlayArrow" to Icons.Default.PlayArrow,
        "Warning" to Icons.Default.Warning,
        "Delete" to Icons.Default.Delete,
        "Add" to Icons.Default.Add,
        "Check" to Icons.Default.Check,
        "Edit" to Icons.Default.Edit,
        "Refresh" to Icons.Default.Refresh,
        "Person" to Icons.Default.Person,
        "Notifications" to Icons.Default.Notifications,
        "Email" to Icons.Default.Email,
        "Search" to Icons.Default.Search,
        "Share" to Icons.Default.Share,
        "ThumbUp" to Icons.Default.ThumbUp,
        "Close" to Icons.Default.Close,
        "Done" to Icons.Default.Done,
        "LocationOn" to Icons.Default.LocationOn,
        "Lock" to Icons.Default.Lock,
        "Build" to Icons.Default.Build,
        "Call" to Icons.Default.Call,
        "Face" to Icons.Default.Face,
        "List" to Icons.Default.List,
        "ExitToApp" to Icons.Default.ExitToApp,
        "AccountCircle" to Icons.Default.AccountCircle,
        "AccountBox" to Icons.Default.AccountBox,
        "Menu" to Icons.Default.Menu,
        "Create" to Icons.Default.Create,
        "Send" to Icons.Default.Send
    )

    fun getIcon(name: String): ImageVector {
        return iconsMap[name] ?: Icons.Default.Add
    }
}
