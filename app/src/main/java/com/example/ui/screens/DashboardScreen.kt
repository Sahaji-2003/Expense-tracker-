package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.UserBudget
import com.example.ui.components.IconMapping
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekMutedText
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekDivider
import com.example.ui.theme.IndigoDarkAccent
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val budget by viewModel.userBudget.collectAsState()

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Date formatting for header
    val currentMonthName = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase()
    }

    // Get timestamp boundaries for current month
    val startOfMonth = remember {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    // Calculations
    val monthExpenses = remember(allExpenses) {
        allExpenses.filter { it.timestamp >= startOfMonth }
    }
    val totalSpentThisMonth = remember(monthExpenses) {
        monthExpenses.sumOf { it.amount }
    }
    val availableBalance = remember(budget.monthlySalary, totalSpentThisMonth) {
        budget.monthlySalary - totalSpentThisMonth
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header & Month (Sleek design)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentMonthName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = SleekMutedText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }

                // Avatar JD/SC with subtle border and inner light background glow
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IndigoDarkAccent.copy(alpha = 0.15f))
                        .border(1.dp, IndigoDarkAccent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SC",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC7D2FE)
                        )
                    )
                }
            }
        }

        // Available Balance Gradient Card (Sleek Design)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                        )
                    )
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(32.dp))
                    .testTag("balance_card")
            ) {
                // Background radial glow effect in corner
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(IndigoDarkAccent.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Available Balance",
                        color = SleekTextSecondary,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = currencyFormatter.format(availableBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Budget status chip (Under budget / Overdraft)
                        val isUnderBudget = availableBalance >= 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (isUnderBudget) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else Color(0xFFEF4444).copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (isUnderBudget) Color(0xFF10B981).copy(alpha = 0.25f)
                                    else Color(0xFFEF4444).copy(alpha = 0.25f),
                                    RoundedCornerShape(50.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isUnderBudget) Color(0xFF10B981) else Color(0xFFEF4444))
                                )
                                Text(
                                    text = if (isUnderBudget) "UNDER BUDGET" else "OVER BUDGDET",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isUnderBudget) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            }
                        }

                        // Salary budget state chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color(0x0CFFFFFF))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Salary: ${currencyFormatter.format(budget.monthlySalary)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = SleekTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Active Budget Categories Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE BUDGETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekMutedText
                )
            }
        }

        if (categories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No categories available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Add custom categories in the Manage Tab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            items(categories) { category ->
                val categorySpent = remember(monthExpenses, category.id) {
                    monthExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
                }
                
                CategoryBudgetCard(
                    category = category,
                    spent = categorySpent,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // Recent Transactions Header
        item {
            Text(
                text = "Recent Logs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (recentExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No expenses logged yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Tap 'Add' below to write your first expense.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            items(recentExpenses, key = { it.id }) { expense ->
                RecentExpenseCard(
                    expense = expense,
                    currencyFormatter = currencyFormatter,
                    onDelete = { viewModel.deleteExpense(expense.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(
    category: Category,
    spent: Double,
    currencyFormatter: NumberFormat
) {
    val consumptionPercent = if (category.monthlyLimit > 0) (spent / category.monthlyLimit).toFloat() else 0f
    
    // Smooth transition for changing indicator colors
    val progressColor by animateColorAsState(
        targetValue = when {
            consumptionPercent < 0.70f -> Color(0xFF10B981) // Emerald Green
            consumptionPercent <= 0.90f -> Color(0xFFF59E0B) // Amber Orange
            else -> Color(0xFFEF4444) // Intense Red
        },
        animationSpec = spring(),
        label = "progressColorSpec"
    )

    val colorAccent = remember(category.colorHex) {
        try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
            .testTag("category_card_${category.id}"),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconMapping.getIcon(category.iconName),
                            contentDescription = category.name,
                            tint = colorAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${(consumptionPercent * 100).toInt()}% consumed",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(spent),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (spent > category.monthlyLimit) Color(0xFFEF4444) else Color.White
                    )
                    Text(
                        text = "Limit: ${currencyFormatter.format(category.monthlyLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { consumptionPercent.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = Color(0x0CFFFFFF)
            )
        }
    }
}

@Composable
fun RecentExpenseCard(
    expense: ExpenseWithCategory,
    currencyFormatter: NumberFormat,
    onDelete: () -> Unit
) {
    val categoryColor = remember(expense.categoryColorHex) {
        try { Color(android.graphics.Color.parseColor(expense.categoryColorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
    }

    val dateString = remember(expense.timestamp) {
        val date = Date(expense.timestamp)
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(date)
    }

    var showConfirmDelete by remember { mutableStateOf(false) }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to remove this transaction for ${currencyFormatter.format(expense.amount)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirmDelete = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showConfirmDelete = true }
            .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(12.dp))
            .testTag("expense_card_${expense.id}"),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Sleek left indicator bar matching recent logs theme in design html
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (expense.description.isBlank()) expense.categoryName else expense.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${expense.categoryName} • $dateString • ${expense.paymentMode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${currencyFormatter.format(expense.amount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(end = 6.dp)
                )
                
                IconButton(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = SleekMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
