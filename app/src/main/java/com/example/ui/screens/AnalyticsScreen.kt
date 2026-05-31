package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekMutedText
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekDivider
import com.example.ui.theme.IndigoDarkAccent
import com.example.ui.theme.PrimaryDark
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2

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
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Time calculations
    val now = remember(allExpenses) { System.currentTimeMillis() }
    
    val startEpoch = remember(timeFrame, now) {
        val calendar = Calendar.getInstance()
        when (timeFrame) {
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            else -> 0L // All time
        }
    }

    // Filtered transaction logs
    val filteredExpenses = remember(allExpenses, startEpoch) {
        if (startEpoch == 0L) {
            allExpenses
        } else {
            allExpenses.filter { it.timestamp >= startEpoch }
        }
    }

    val totalSpent = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    // Spend distribution by category
    val categoryDistribution = remember(filteredExpenses, categories) {
        categories.map { category ->
            val spent = filteredExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            CategorySpendShare(
                category = category,
                spentAmount = spent,
                percent = if (totalSpent > 0) (spent / totalSpent).toFloat() else 0f
            )
        }.filter { it.spentAmount > 0 }.sortedByDescending { it.spentAmount }
    }

    // Retrieve active selection
    val activeSelection = remember(selectedCategoryId, categoryDistribution) {
        categoryDistribution.firstOrNull { it.category.id == selectedCategoryId }
    }

    // Details filtering
    val detailsExpenses = remember(filteredExpenses, selectedCategoryId) {
        if (selectedCategoryId == null) {
            filteredExpenses
        } else {
            filteredExpenses.filter { it.categoryId == selectedCategoryId }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle timeframe (Sleek design)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekDivider, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("This Week", "This Month", "All Time").forEach { opt ->
                    val isSelected = timeFrame == opt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) IndigoDarkAccent else Color.Transparent)
                            .clickable {
                                viewModel.setTimeFrame(opt)
                            }
                            .padding(vertical = 10.dp),
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
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = PrimaryDark.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No logs in $timeFrame timeframe",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Add some test categories or log entries in the Add Tab to analyze details.",
                            style = MaterialTheme.typography.bodyMedium,
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
                            text = "Expense Proportions Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
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

            // Category list summary
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
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(item.spentAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
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
            if (timeFrame == "This Month") {
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
                                    contentDescription = null,
                                    tint = if (isUnderBudget) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isUnderBudget) "Trending Under Limits" else "Budget Overdraft Alert",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
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

            // Historical Segment Logs list (re-filtered contextual category logs)
            item {
                Text(
                    text = if (selectedCategoryId == null) "HISTORICAL LOGS" else "LOGS FOR SELECTED SEGMENT",
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
                            // Sleek left indicator bar matching recent logs theme in design html
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
                                    color = Color.White,
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
    Box(
        modifier = Modifier
            .size(240.dp)
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 32.dp.toPx()
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = (minOf(canvasWidth, canvasHeight) - strokeWidth) / 2f
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            var lastAngle = -90f // Start angles at exactly 12 o'clock top orientation

            distributions.forEach { share ->
                val sweepAngle = share.percent * 360f
                val colorAccent = try {
                    Color(android.graphics.Color.parseColor(share.category.colorHex))
                } catch (e: java.lang.Exception) {
                    Color(0xFF64748B)
                }

                // If segment selected, draw it with expanded stroke style
                val isSelected = selectedId == share.category.id
                val finalStrokeWidth = if (isSelected) strokeWidth + 12.dp.toPx() else strokeWidth

                drawArc(
                    color = colorAccent,
                    startAngle = lastAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = finalStrokeWidth, cap = StrokeCap.Round)
                )

                lastAngle += sweepAngle
            }

            // Draw a fallback gray circle if there are no distributions
            if (distributions.isEmpty()) {
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

        // Draw central dynamic label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currencyFormatter.format(displayAmount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (selectedId != null) {
                Text(
                    text = "Tap to Reset",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onSegmentSelect(null) }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
