package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun MainContainerScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Dashboard") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation"),
                containerColor = com.example.ui.theme.SleekBg,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "Dashboard",
                    onClick = { selectedTab = "Dashboard" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Dashboard overview"
                        )
                    },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PrimaryDark,
                        selectedTextColor = com.example.ui.theme.PrimaryDark,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary,
                        indicatorColor = com.example.ui.theme.SleekSurface
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = selectedTab == "Add / Manage",
                    onClick = { selectedTab = "Add / Manage" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Transaction and category inputs manager"
                        )
                    },
                    label = { Text("Add / Manage") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PrimaryDark,
                        selectedTextColor = com.example.ui.theme.PrimaryDark,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary,
                        indicatorColor = com.example.ui.theme.SleekSurface
                    ),
                    modifier = Modifier.testTag("nav_tab_add")
                )

                NavigationBarItem(
                    selected = selectedTab == "Analytics",
                    onClick = { selectedTab = "Analytics" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Visual proportions and insights charts"
                        )
                    },
                    label = { Text("Analytics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PrimaryDark,
                        selectedTextColor = com.example.ui.theme.PrimaryDark,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary,
                        indicatorColor = com.example.ui.theme.SleekSurface
                    ),
                    modifier = Modifier.testTag("nav_tab_analytics")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "Dashboard" -> DashboardScreen(viewModel = viewModel)
                "Add / Manage" -> AddManageHubScreen(viewModel = viewModel)
                "Analytics" -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }
}
