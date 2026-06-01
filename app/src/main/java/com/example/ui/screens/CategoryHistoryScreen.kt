package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.ExpenseWithCategory
import com.example.ui.components.IconMapping
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekMutedText
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHistoryScreen(
    category: Category,
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allExpenses by viewModel.allExpenses.collectAsState()

    val currencyFormatter = remember {
        try {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }

    // Current month start epoch boundary
    val startOfMonth = remember {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    // Filter current month transactions belonging strictly to this category
    val categoryExpensesThisMonth = remember(allExpenses, category.id) {
        allExpenses.filter { it.categoryId == category.id && it.timestamp >= startOfMonth }
    }

    val totalSpent = remember(categoryExpensesThisMonth) {
        categoryExpensesThisMonth.sumOf { it.amount }
    }

    val consumptionPercent = if (category.monthlyLimit > 0) (totalSpent / category.monthlyLimit).toFloat() else 0f

    val progressColor by animateColorAsState(
        targetValue = when {
            consumptionPercent < 0.70f -> Color(0xFF10B981)
            consumptionPercent <= 0.90f -> Color(0xFFF59E0B)
            else -> Color(0xFFEF4444)
        },
        animationSpec = spring(),
        label = "progressBarColor"
    )

    val colorAccent = remember(category.colorHex) {
        try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
    }

    val monthName = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("category_history_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = SleekTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = SleekTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Category info overview card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconMapping.getIcon(category.iconName),
                                    contentDescription = category.name,
                                    tint = colorAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "$monthName Budget Status",
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = SleekTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Spent", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                                Text(
                                    text = currencyFormatter.format(totalSpent),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (totalSpent > category.monthlyLimit) Color(0xFFEF4444) else SleekTextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Allocated Limit", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                                Text(
                                    text = currencyFormatter.format(category.monthlyLimit),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = SleekTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { consumptionPercent.coerceAtMost(1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = progressColor,
                            trackColor = Color(0x0CFFFFFF)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Surplus/overdraft indicator message
                        if (totalSpent > category.monthlyLimit) {
                            Text(
                                text = "Overdraft by ${currencyFormatter.format(totalSpent - category.monthlyLimit)}! Please trim other categories.",
                                color = Color(0xFFF87171),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        } else {
                            Text(
                                text = "Safe! Remaining surplus: ${currencyFormatter.format(category.monthlyLimit - totalSpent)}",
                                color = Color(0xFF34D399),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            // Month timeline title
            item {
                Text(
                    text = "CURRENT MONTH TRANSACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekMutedText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (categoryExpensesThisMonth.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty state icon",
                            tint = SleekMutedText,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No logs recorded for ${category.name} in $monthName.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(categoryExpensesThisMonth) { expense ->
                    RecentExpenseCard(
                        expense = expense,
                        currencyFormatter = currencyFormatter,
                        onDelete = { viewModel.deleteExpense(expense.id) }
                    )
                }
            }
        }
    }
}
