package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun MainContainerScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val isSessionLoading by viewModel.isSessionLoading.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    GlowingBackground(isDark = isDarkTheme) {
        if (isSessionLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = com.example.ui.theme.PrimaryDark)
            }
        } else if (loggedInUser == null) {
            LoginScreen(viewModel = viewModel, modifier = modifier)
        } else {
            var selectedTab by remember { mutableStateOf("Dashboard") }

            Scaffold(
                modifier = modifier.fillMaxSize().testTag("main_container_screen"),
                containerColor = Color.Transparent, // Let glowing background render behind scaffold
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_navigation"),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        tonalElevation = 4.dp
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
                                indicatorColor = com.example.ui.theme.PrimaryDark.copy(alpha = 0.15f)
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
                                indicatorColor = com.example.ui.theme.PrimaryDark.copy(alpha = 0.15f)
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
                                indicatorColor = com.example.ui.theme.PrimaryDark.copy(alpha = 0.15f)
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
                        "Dashboard" -> DashboardScreen(
                            viewModel = viewModel,
                            onProfileClick = { selectedTab = "Profile" }
                        )
                        "Add / Manage" -> AddHubScreenWrapperValue(viewModel = viewModel)
                        "Analytics" -> AnalyticsScreen(viewModel = viewModel)
                        "Profile" -> ProfileScreen(
                            viewModel = viewModel,
                            onBack = { selectedTab = "Dashboard" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddHubScreenWrapperValue(viewModel: ExpenseViewModel) {
    AddManageHubScreen(viewModel = viewModel)
}

@Composable
fun GlowingBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowing_blobs_transition")

    // Normalized floating translation percentages across screen coordinate space
    val blob1XPct by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "normalizedBlob1X"
    )
    val blob1YPct by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "normalizedBlob1Y"
    )

    val blob2XPct by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = -0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "normalizedBlob2X"
    )
    val blob2YPct by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "normalizedBlob2Y"
    )

    // Vibrant neon accents with tuned opacity for higher visual visibility and glow depth
    val color1 = if (isDark) Color(0xFF6366F1).copy(alpha = 0.36f) else Color(0xFFEC4899).copy(alpha = 0.28f)
    val color2 = if (isDark) Color(0xFF10B981).copy(alpha = 0.32f) else Color(0xFF3B82F6).copy(alpha = 0.26f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val blob1X = blob1XPct * width
            val blob1Y = blob1YPct * height
            val radius1 = width * 0.85f

            val blob2X = blob2XPct * width
            val blob2Y = blob2YPct * height
            val radius2 = width * 0.95f

            // Smoothly render ambient radial blending circles
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1, Color.Transparent),
                    center = Offset(blob1X, blob1Y),
                    radius = radius1
                ),
                radius = radius1,
                center = Offset(blob1X, blob1Y)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2, Color.Transparent),
                    center = Offset(blob2X, blob2Y),
                    radius = radius2
                ),
                radius = radius2,
                center = Offset(blob2X, blob2Y)
            )
        }

        // Overlay screen contents perfectly
        content()
    }
}
