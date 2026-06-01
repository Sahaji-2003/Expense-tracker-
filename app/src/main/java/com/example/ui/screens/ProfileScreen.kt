package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val budget by viewModel.userBudget.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    var showSignOutAlert by remember { mutableStateOf(false) }
    var showEditSalaryDialog by remember { mutableStateOf(false) }
    var salaryInput by remember { mutableStateOf("") }

    val currencyFormatter = remember {
        try {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }

    if (showSignOutAlert) {
        AlertDialog(
            onDismissRequest = { showSignOutAlert = false },
            title = { Text("Sign Out Session?", color = Color.White) },
            text = { Text("Do you want to lock this vault? You can log back in anytime using your secret PIN.", color = SleekTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutAlert = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutAlert = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface
        )
    }

    var showSavedFeedback by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showEditSalaryDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!showSavedFeedback) {
                    showEditSalaryDialog = false 
                }
            },
            title = { Text("Update Monthly Budget", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Set your monthly earnings/budget limit. All progress bars will automatically scale to this amount.", color = SleekTextSecondary, fontSize = 14.sp)
                    OutlinedTextField(
                        value = salaryInput,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]{0,2}$"))) {
                                salaryInput = it
                            }
                        },
                        label = { Text("Monthly Earnings / Salary") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = SleekDivider,
                            focusedLabelColor = PrimaryDark,
                            unfocusedLabelColor = SleekTextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("profile_salary_input"),
                        enabled = !showSavedFeedback
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = salaryInput.toDoubleOrNull()
                        if (amount != null && amount > 0 && !showSavedFeedback) {
                            scope.launch {
                                showSavedFeedback = true
                                viewModel.updateSalary(amount)
                                kotlinx.coroutines.delay(1200)
                                showSavedFeedback = false
                                showEditSalaryDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (showSavedFeedback) Color(0xFF10B981) else PrimaryDark
                    ),
                    enabled = !showSavedFeedback
                ) {
                    if (showSavedFeedback) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Saved ✓", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditSalaryDialog = false },
                    enabled = !showSavedFeedback
                ) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper display title row with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SleekSurface)
                    .border(1.dp, SleekDivider, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back to dashboard",
                    tint = Color.White
                )
            }
            Text(
                text = "Profile Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f) // Ensures text handles wrapping responsively
            )
        }

        // Core Profile Identity Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SleekDivider, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Avatar with User Initials
                val userInitials = remember(loggedInUser) {
                    val name = loggedInUser?.name?.trim() ?: "Guest"
                    val parts = name.split(" ")
                    if (parts.size >= 2) {
                        "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                    } else {
                        name.take(2).uppercase()
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(IndigoDarkAccent.copy(alpha = 0.2f))
                        .border(2.dp, IndigoDarkAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC7D2FE)
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = loggedInUser?.name ?: "Personalized User",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "@${loggedInUser?.username ?: "guest_user"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekMutedText
                    )
                }

                Divider(color = SleekDivider, thickness = 1.dp)

                // Salary / Monthly Limit Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Budget Limit",
                            style = MaterialTheme.typography.labelMedium,
                            color = SleekTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currencyFormatter.format(budget.monthlySalary),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryDark
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            salaryInput = budget.monthlySalary.toString()
                            showEditSalaryDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekDivider)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit budget", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", fontSize = 12.sp)
                    }
                }
            }
        }

        // Vault & Security metadata
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SleekDivider, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Vault Details",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Security Status", color = SleekTextSecondary, fontSize = 14.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "secured", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Text("PIN-Protected (AES-256)", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Database Storage", color = SleekTextSecondary, fontSize = 14.sp)
                    Text("SQLite Room Persistent", color = Color.White, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Logged Transactions", color = SleekTextSecondary, fontSize = 14.sp)
                    Text("${allExpenses.size} items", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Red Session Log Out button
        Button(
            onClick = { showSignOutAlert = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("profile_logout_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                contentColor = Color(0xFFEF4444)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log out icon")
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Log Out of Session",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
