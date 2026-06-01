package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.ExpenseWithCategory
import com.example.ui.components.IconMapping
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val timeFrame by viewModel.timeFrame.collectAsState()
    val selectedCategoryId by viewModel.selectedAnalyticsCategoryId.collectAsState()

    val haptic = LocalHapticFeedback.current
    val currencyFormatter = remember {
        try {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }

    val now = remember(allExpenses) { System.currentTimeMillis() }

    // Multi-month timeframe boundaries logic
    val timeframeBoundaries = remember(timeFrame, now) {
        val calendar = Calendar.getInstance()
        var startTime = 0L
        var endTime = Long.MAX_VALUE
        
        when (timeFrame) {
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }
            "This Month", "Current Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }
            "Last Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endTime = calendar.timeInMillis
            }
            "2 Months Ago" -> {
                calendar.add(Calendar.MONTH, -2)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endTime = calendar.timeInMillis
            }
            "3 Months Ago" -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endTime = calendar.timeInMillis
            }
            else -> {
                startTime = 0L
                endTime = Long.MAX_VALUE
            }
        }
        Pair(startTime, endTime)
    }

    val startEpoch = timeframeBoundaries.first
    val endEpoch = timeframeBoundaries.second

    // Query historical filter sets
    val filteredExpenses = remember(allExpenses, startEpoch, endEpoch) {
        allExpenses.filter { it.timestamp >= startEpoch && it.timestamp <= endEpoch }
    }

    val totalSpent = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    // Pie slice calculations
    val categoryDistribution = remember(filteredExpenses, categories) {
        categories.map { category ->
            val spent = filteredExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            CategorySpendShare(
                category = category,
                spentAmount = spent,
                percent = if (totalSpent > 0f) (spent / totalSpent).toFloat() else 0f
            )
        }.filter { it.spentAmount > 0 }.sortedByDescending { it.spentAmount }
    }

    val activeSelection = remember(selectedCategoryId, categoryDistribution) {
        categoryDistribution.firstOrNull { it.category.id == selectedCategoryId }
    }

    val detailsExpenses = remember(filteredExpenses, selectedCategoryId) {
        if (selectedCategoryId == null) {
            filteredExpenses
        } else {
            filteredExpenses.filter { it.categoryId == selectedCategoryId }
        }
    }

    // Compute previous last 3 months boundaries (Month -1, -2, -3) 
    val monthCompareStats = remember(allExpenses, categories) {
        val statsList = mutableListOf<CategoryComparisonStat>()
        val calendar = Calendar.getInstance()

        // Month 0 (Current Month) starting threshold
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val m0Start = calendar.timeInMillis

        // Month -1 threshold
        calendar.add(Calendar.MONTH, -1)
        val m1Start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val m1End = calendar.timeInMillis

        // Month -2 threshold
        calendar.add(Calendar.MONTH, -1)
        val m2Start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val m2End = calendar.timeInMillis

        // Month -3 threshold
        calendar.add(Calendar.MONTH, -1)
        val m3Start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val m3End = calendar.timeInMillis

        for (cat in categories) {
            val spentThisMonth = allExpenses.filter { it.categoryId == cat.id && it.timestamp >= m0Start }.sumOf { it.amount }
            
            val spentM1 = allExpenses.filter { it.categoryId == cat.id && it.timestamp >= m1Start && it.timestamp <= m1End }.sumOf { it.amount }
            val spentM2 = allExpenses.filter { it.categoryId == cat.id && it.timestamp >= m2Start && it.timestamp <= m2End }.sumOf { it.amount }
            val spentM3 = allExpenses.filter { it.categoryId == cat.id && it.timestamp >= m3Start && it.timestamp <= m3End }.sumOf { it.amount }

            // Average of previous 3 months
            val threeMonthsSpentSum = spentM1 + spentM2 + spentM3
            val averagePast3Months = threeMonthsSpentSum / 3.0

            statsList.add(
                CategoryComparisonStat(
                    category = cat,
                    spentCurrentMonth = spentThisMonth,
                    averagePast3Months = averagePast3Months,
                    trendPercentage = if (averagePast3Months > 0) {
                        ((spentThisMonth - averagePast3Months) / averagePast3Months * 100).toInt()
                    } else 0
                )
            )
        }
        statsList
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle timeframe row (with horizontally scrollable support to fit past months gracefully)
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekDivider, RoundedCornerShape(14.dp))
                    .horizontalScroll(scrollState)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("This Week", "This Month", "Last Month", "2 Months Ago", "3 Months Ago").forEach { opt ->
                    val isSelected = timeFrame == opt || (opt == "This Month" && timeFrame == "Current Month")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) IndigoDarkAccent else Color.Transparent)
                            .clickable {
                                viewModel.setTimeFrame(if (opt == "This Month") "Current Month" else opt)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else SleekTextSecondary
                        )
                    }
                }
            }
        }

        if (filteredExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty icon warning",
                            modifier = Modifier.size(48.dp),
                            tint = PrimaryDark.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No logs in selected timeframe",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = SleekTextPrimary),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Expenses logged during this calendar month will be plotted here. Reset is automated on month-boundary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Interactive donut chart card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (timeFrame == "Current Month" || timeFrame == "This Month") "Current Spending Proportions" else "$timeFrame Proportions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekTextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        InteractiveDonutChart(
                            distributions = categoryDistribution,
                            totalSpent = totalSpent,
                            selectedId = selectedCategoryId,
                            currencyFormatter = currencyFormatter,
                            onSegmentSelect = { selectedId ->
                                viewModel.setSelectedAnalyticsCategory(selectedId)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected status subtitle inside analytics screen
                        AnimatedVisibility(
                            visible = selectedCategoryId != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IndigoDarkAccent.copy(alpha = 0.15f))
                                    .border(1.dp, IndigoDarkAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSelectedAnalyticsCategory(null) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Filtered by: ${activeSelection?.category?.name ?: ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC7D2FE)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✕",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC7D2FE)
                                )
                            }
                        }
                    }
                }
            }

            // Category list summary shares
            item {
                Text(
                    text = "CATEGORY DISTRIBUTION SHARES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = SleekMutedText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(categoryDistribution) { item ->
                val isSelected = selectedCategoryId == item.category.id
                val colorAccent = remember(item.category.colorHex) {
                    try { Color(android.graphics.Color.parseColor(item.category.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isSelected) IndigoDarkAccent.copy(alpha = 0.4f) else SleekDivider,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.setSelectedAnalyticsCategory(if (isSelected) null else item.category.id)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IndigoDarkAccent.copy(alpha = 0.12f) else SleekSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colorAccent)
                             )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.category.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekTextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(item.spentAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "${(item.percent * 100).toInt()}% of total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Delta analysis report (Only when time selection features "This Month")
            if (timeFrame == "Current Month" || timeFrame == "This Month") {
                item {
                    Text(
                        text = "MONTHLY DELTA BUDGET ANALYSIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = SleekMutedText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    val totalBudgetLimits = remember(categories) { categories.sumOf { it.monthlyLimit } }
                    val isUnderBudget = totalSpent < totalBudgetLimits
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SleekDivider, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SleekSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnderBudget) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "info badge status",
                                    tint = if (isUnderBudget) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isUnderBudget) "Trending Under Limits" else "Budget Overdraft Alert",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = if (isUnderBudget) {
                                        "You are doing great! You are ₹${currencyFormatter.format(totalBudgetLimits - totalSpent)} under your total category allocation limits."
                                    } else {
                                        "Warning: You spent ₹${currencyFormatter.format(totalSpent - totalBudgetLimits)} more than your designated category budget thresholds!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3-Month Comparative Trend Analysis Screen Category Table
            item {
                Text(
                    text = "3-MONTH COMPARATIVE TREND SENSITIVITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = SleekMutedText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spent This Month vs 3-Month Hist. Average",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        monthCompareStats.forEach { stat ->
                            val colorAccent = try { Color(android.graphics.Color.parseColor(stat.category.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
                            val isHigher = stat.spentCurrentMonth > stat.averagePast3Months
                            val isUnchanged = stat.spentCurrentMonth == stat.averagePast3Months

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.3f)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorAccent))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stat.category.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = SleekTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currencyFormatter.format(stat.spentCurrentMonth),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "Avg: ${currencyFormatter.format(stat.averagePast3Months)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekMutedText,
                                        fontSize = 10.sp
                                    )
                                }

                                // Trend indicator badging
                                Box(
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .padding(start = 12.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    val badgeColor = when {
                                        isUnchanged -> Color.Gray.copy(alpha = 0.15f)
                                        isHigher -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                        else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    }
                                    val textColor = when {
                                        isUnchanged -> Color.Gray
                                        isHigher -> Color(0xFFF87171)
                                        else -> Color(0xFF34D399)
                                    }
                                    val textValue = when {
                                        isUnchanged -> "0%"
                                        isHigher -> "+${stat.trendPercentage}%"
                                        else -> "-${stat.trendPercentage.coerceAtLeast(-100)}%"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = textValue,
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Historical segment logs list 
            item {
                Text(
                    text = if (selectedCategoryId == null) "TIME FRAME LOGS" else "LOGS FOR SELECTED CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = SleekMutedText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(detailsExpenses) { log ->
                val accentColor = remember(log.categoryColorHex) {
                    try { Color(android.graphics.Color.parseColor(log.categoryColorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
                }

                val dateString = remember(log.timestamp) {
                    val date = Date(log.timestamp)
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(date)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(28.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (log.description.isNotBlank()) log.description else log.categoryName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${log.categoryName} • $dateString • ${log.paymentMode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "-${currencyFormatter.format(log.amount)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// Comparison Helper data class
private data class CategoryComparisonStat(
    val category: Category,
    val spentCurrentMonth: Double,
    val averagePast3Months: Double,
    val trendPercentage: Int
)

data class CategorySpendShare(
    val category: Category,
    val spentAmount: Double,
    val percent: Float
)

@Composable
fun InteractiveDonutChart(
    distributions: List<CategorySpendShare>,
    totalSpent: Double,
    selectedId: Long?,
    currencyFormatter: NumberFormat,
    onSegmentSelect: (Long?) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    // Animate the sweep sectors on entry or when data changes
    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(distributions) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Capture stroke widths reactively per category piece with stabilized composition keys
    val animatedStrokeWidths = distributions.map { share ->
        key(share.category.id) {
            val isSelected = selectedId == share.category.id
            val targetWidthDp = if (isSelected) 44.dp else 32.dp
            val targetWidthPx = with(density) { targetWidthDp.toPx() }
            animateFloatAsState(
                targetValue = targetWidthPx,
                animationSpec = spring(
                    dampingRatio = 0.62f,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "stroke_width_${share.category.id}"
            )
        }
    }

    Box(
        modifier = Modifier
            .size(240.dp)
            .drawBehind {
                val cw = size.width
                val ch = size.height
                val centerOffset = Offset(cw / 2f, ch / 2f)
                // Subtle radial cosmic glow under the donut chart
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1F818CF8),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = cw / 1.4f
                    ),
                    radius = cw / 1.4f,
                    center = centerOffset
                )
            }
            .pointerInput(distributions) {
                // Approximate angle detection on canvas click
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    
                    // Angle in degrees from top (clockwise)
                    var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    touchAngle += 90f // align starting at 12 o'clock
                    if (touchAngle < 0f) touchAngle += 360f

                    // Map angle back to segment
                    var currentAngleSum = 0f
                    var clickedId: Long? = null

                    for (item in distributions) {
                        val sweep = item.percent * 360f
                        if (touchAngle >= currentAngleSum && touchAngle <= (currentAngleSum + sweep)) {
                            clickedId = item.category.id
                            break
                        }
                        currentAngleSum += sweep
                    }

                    if (clickedId != null) {
                        onSegmentSelect(if (selectedId == clickedId) null else clickedId)
                    } else {
                        onSegmentSelect(null)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_donut_chart")
        ) {
            val strokeWidth = with(density) { 32.dp.toPx() }
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            var lastAngle = -90f // Start angles at exactly 12 o'clock top orientation

            distributions.forEachIndexed { index, share ->
                val sweepAngle = share.percent * 360f * transitionProgress.value
                val colorAccent = try {
                    Color(android.graphics.Color.parseColor(share.category.colorHex))
                } catch (e: java.lang.Exception) {
                    Color(0xFF64748B)
                }

                // If segment selected, draw it with expanded stroke style
                val finalStrokeWidth = animatedStrokeWidths.getOrNull(index)?.value ?: strokeWidth
                val radius = (minOf(canvasWidth, canvasHeight) - finalStrokeWidth) / 2f

                drawArc(
                    color = colorAccent,
                    startAngle = lastAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = finalStrokeWidth, cap = StrokeCap.Round)
                )

                // Only draw text labels when animation has advanced enough and the share isn't tiny
                if (transitionProgress.value > 0.4f && share.percent >= 0.04f && sweepAngle > 10f) {
                    val middleAngle = lastAngle + sweepAngle / 2f
                    val middleAngleRad = Math.toRadians(middleAngle.toDouble())
                    
                    // Center percentage in the middle of the segment stroke band
                    val textRadius = radius
                    val textX = center.x + textRadius * cos(middleAngleRad).toFloat()
                    val textY = center.y + textRadius * sin(middleAngleRad).toFloat()
                    
                    val percentText = "${(share.percent * 100).roundToInt()}%"
                    
                    drawIntoCanvas { canvas ->
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = with(density) { 11.sp.toPx() }
                            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                            textAlign = android.graphics.Paint.Align.CENTER
                            // Ambient dark shadow underneath text for crisp readability
                            setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(220, 0, 0, 0))
                        }
                        
                        // Vertical centering math using FontMetrics
                        val fontMetrics = textPaint.fontMetrics
                        val textHeightOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
                        
                        canvas.nativeCanvas.drawText(
                            percentText,
                            textX,
                            textY - textHeightOffset,
                            textPaint
                        )
                    }
                }

                lastAngle += sweepAngle
            }

            // Draw a fallback gray circle if there are no distributions
            if (distributions.isEmpty()) {
                val radius = (minOf(canvasWidth, canvasHeight) - strokeWidth) / 2f
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Concentric translucent card for depth
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(CircleShape)
                .background(Color(0x3B0B0B14))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x4D818CF8),
                            Color(0x1A1E1B4B)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Draw central dynamic label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                val displayLabel = if (selectedId != null) {
                    distributions.firstOrNull { it.category.id == selectedId }?.category?.name ?: "Total Spend"
                } else {
                    "Total Spend"
                }

                val displayAmount = if (selectedId != null) {
                    distributions.firstOrNull { it.category.id == selectedId }?.spentAmount ?: totalSpent
                } else {
                    totalSpent
                }

                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormatter.format(displayAmount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = SleekTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (selectedId != null) {
                    Text(
                        text = "Reset Filter",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF818CF8),
                        modifier = Modifier
                            .clickable { onSegmentSelect(null) }
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
