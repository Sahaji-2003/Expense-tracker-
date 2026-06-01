package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.ExpenseRepository
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekMutedText
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.PrimaryDark
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var monthlySalary by remember { mutableStateOf("") }

    val loginError by viewModel.loginError.collectAsState()
    val registerStatus by viewModel.registerStatus.collectAsState()
    val scope = rememberCoroutineScope()

    // Clear statuses on mode toggle
    LaunchedEffect(isRegisterMode) {
        viewModel.clearAuthStatus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("login_screen"),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                            listOf(SleekBg, Color(0xFF161618))
                        } else {
                            listOf(Color.Transparent, Color.Transparent)
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background decor
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-80).dp, y = (-80).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryDark.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Large styled branding logo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryDark, Color(0xFF4F46E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "App Key Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expense Vault",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRegisterMode) "Create a secure workspace" else "Sign in to your private vault",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekMutedText,
                        textAlign = TextAlign.Center
                    )
                }

                // Modes Tab row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekSurface)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isRegisterMode) SleekSurfaceVariant else Color.Transparent)
                            .clickable { isRegisterMode = false }
                            .padding(vertical = 10.dp)
                            .testTag("tab_login_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log In",
                            fontWeight = FontWeight.Bold,
                            color = if (!isRegisterMode) SleekTextPrimary else SleekMutedText,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRegisterMode) SleekSurfaceVariant else Color.Transparent)
                            .clickable { isRegisterMode = true }
                            .padding(vertical = 10.dp)
                            .testTag("tab_register_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            fontWeight = FontWeight.Bold,
                            color = if (isRegisterMode) SleekTextPrimary else SleekMutedText,
                            fontSize = 14.sp
                        )
                    }
                }

                // Fields area inside structured container
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.elevatedCardColors(containerColor = SleekSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isRegisterMode) {
                            // Full Name
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon", tint = SleekTextSecondary) },
                                modifier = Modifier.fillMaxWidth().testTag("name_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryDark,
                                    unfocusedBorderColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0x1AFFFFFF) else Color(0x14000000),
                                    focusedLabelColor = PrimaryDark,
                                    unfocusedLabelColor = SleekMutedText,
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Username 
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username", tint = SleekTextSecondary) },
                            modifier = Modifier.fillMaxWidth().testTag("username_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryDark,
                                unfocusedBorderColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0x1AFFFFFF) else Color(0x14000000),
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekMutedText,
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password / PIN
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { if (it.length <= 8) pin = it },
                            label = { Text("Secret PIN (4-8 digits)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Security PIN Code", tint = SleekTextSecondary) },
                            modifier = Modifier.fillMaxWidth().testTag("pin_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryDark,
                                unfocusedBorderColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0x1AFFFFFF) else Color(0x14000000),
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekMutedText,
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (isRegisterMode) {
                            // Initial Monthly Budget
                            OutlinedTextField(
                                value = monthlySalary,
                                onValueChange = { monthlySalary = it },
                                label = { Text("Initial Monthly Salary / Budget") },
                                leadingIcon = { Text("₹ ", style = MaterialTheme.typography.bodyLarge, color = SleekTextSecondary) },
                                modifier = Modifier.fillMaxWidth().testTag("salary_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryDark,
                                    unfocusedBorderColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0x1AFFFFFF) else Color(0x14000000),
                                    focusedLabelColor = PrimaryDark,
                                    unfocusedLabelColor = SleekMutedText,
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Error alerts
                        val activeError = if (isRegisterMode) registerStatus else loginError
                        if (!activeError.isNullOrBlank() && activeError != "Success") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Alert error badge icon",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = activeError ?: "",
                                    color = Color(0xFFF87171),
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isRegisterMode) {
                                    val salDouble = monthlySalary.toDoubleOrNull() ?: 0.0
                                    viewModel.register(username, pin, name, salDouble)
                                } else {
                                    viewModel.login(username, pin)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_action_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryDark,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isRegisterMode) "Create Account" else "Unlock Session",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Friendly notice about blank stats
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice icon info",
                        tint = SleekMutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New users start with a clean 0 balance sheet workspace.",
                        color = SleekMutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Demo Starter Profile Shortcut link (for immediate evaluation if wanted)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Want a quick look? Try entering the username \"demo\" and PIN \"1234\" (we'll register it with seeded demo categories automatically if needed!)",
                    color = SleekMutedText.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            isRegisterMode = false
                            username = "demo"
                            pin = "1234"
                            scope.launch {
                                val demoAccount = viewModel.getUserSessionDirect("demo")
                                if (demoAccount == null) {
                                    viewModel.register("demo", "1234", "Demo Tester", 65000.0)
                                } else {
                                    viewModel.login("demo", "1234")
                                }
                            }
                        }
                )
            }
        }
    }
}
