package com.example.ui.screens

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.RecurringExpenseWithCategory
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddManageHubScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val budget by viewModel.userBudget.collectAsState()
    val recurringExpenses by viewModel.recurringExpenses.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currencyFormatter = remember {
        try {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }

    // Forms states
    var activeTab by remember { mutableStateOf("Expenses") } // "Expenses" or "Categories" or "Salar_Bills"

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Expense Form States
    var expenseAmount by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedPaymentMode by remember { mutableStateOf("Bank") } // "Cash", "Bank", "Credit Card"
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var showExpenseSavedMessage by remember { mutableStateOf(false) }

    // Category Form States
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("ShoppingCart") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }
    var categoryLimit by remember { mutableStateOf("") }

    var showCategorySavedMessage by remember { mutableStateOf(false) }
    var showBillSavedMessage by remember { mutableStateOf(false) }

    if (showExpenseSavedMessage) {
        LaunchedEffect(showExpenseSavedMessage) {
            delay(3000)
            showExpenseSavedMessage = false
        }
    }

    if (showCategorySavedMessage) {
        LaunchedEffect(showCategorySavedMessage) {
            delay(3000)
            showCategorySavedMessage = false
        }
    }

    var showSalarySavedMessage by remember { mutableStateOf(false) }

    if (showBillSavedMessage) {
        LaunchedEffect(showBillSavedMessage) {
            delay(3000)
            showBillSavedMessage = false
        }
    }

    if (showSalarySavedMessage) {
        LaunchedEffect(showSalarySavedMessage) {
            delay(3000)
            showSalarySavedMessage = false
        }
    }

    // Salary state
    var salaryInput by remember { mutableStateOf("") }
    LaunchedEffect(budget.monthlySalary) {
        if (salaryInput.isEmpty()) {
            salaryInput = budget.monthlySalary.toString()
        }
    }

    // Bill Creator state
    var billName by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var billCategoryId by remember { mutableStateOf<Long?>(null) }
    var billDayOfMonth by remember { mutableStateOf("1") }
    var billAutoDeduct by remember { mutableStateOf(true) }

    // Auto-select first category if list is not empty and selection is currently null
    LaunchedEffect(categories) {
        if (categories.isNotEmpty()) {
            if (selectedCategoryId == null) {
                selectedCategoryId = categories.first().id
            }
            if (billCategoryId == null) {
                billCategoryId = categories.first().id
            }
        }
    }

    // Constants for customizing with 20 premium colors
    val paletteColors = listOf(
        "#3B82F6", // Blue
        "#EF4444", // Red
        "#10B981", // Green/Emerald
        "#F59E0B", // Amber
        "#6366F1", // Indigo
        "#EC4899", // Pink
        "#8B5CF6", // Purple
        "#14B8A6", // Teal
        "#F97316", // Orange
        "#06B6D4", // Cyan
        "#64748B", // Slate/Grey
        "#84CC16", // Lime
        "#22C55E", // Light Green
        "#D946EF", // Fuchsia
        "#E11D48", // Rose
        "#F53267", // Ruby
        "#DFDF11", // Bright Yellow/Olive
        "#2E8B57", // SeaGreen
        "#FF6347", // Tomato
        "#4682B4"  // SteelBlue
    )

    // 30 highly meaningful finance/spending category icons
    val supportedIcons = listOf(
        "Home", "ShoppingCart", "Favorite", "Star", "Settings", "Info", "PlayArrow", "Warning", "Delete", "Add",
        "Check", "Edit", "Refresh", "Person", "Notifications", "Email", "Search", "Share", "ThumbUp", "Close",
        "Done", "LocationOn", "Lock", "Build", "Call", "Face", "List", "ExitToApp", "AccountCircle", "AccountBox"
    )

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("add_manage_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector Bar
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
                listOf("Expenses", "Categories", "Setup & Bills").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) IndigoDarkAccent else Color.Transparent)
                            .clickable {
                                activeTab = tab
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else SleekTextSecondary
                        )
                    }
                }
            }
        }

        // --- EXPENSES LOG TAB ---
        if (activeTab == "Expenses") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Add Transaction",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        // Float Currency Input Amount
                        Column {
                            Text(
                                text = "Spent Amount",
                                style = MaterialTheme.typography.labelMedium,
                                color = SleekTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = expenseAmount,
                                onValueChange = { 
                                    if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]{0,2}$"))) {
                                        expenseAmount = it
                                    }
                                },
                                leadingIcon = {
                                    Text(
                                        text = "₹",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryDark
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = SleekTextPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("expense_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary,
                                    focusedContainerColor = SleekSurfaceVariant,
                                    unfocusedContainerColor = SleekSurfaceVariant,
                                    focusedBorderColor = IndigoDarkAccent,
                                    unfocusedBorderColor = SleekDivider,
                                    focusedPlaceholderColor = SleekMutedText,
                                    unfocusedPlaceholderColor = SleekMutedText
                                ),
                                placeholder = {
                                    Text(
                                        text = "0.00",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SleekMutedText
                                    )
                                }
                            )
                        }

                        // Description TextField
                        OutlinedTextField(
                            value = expenseDescription,
                            onValueChange = { expenseDescription = it },
                            label = { Text("Notes / Description (Optional)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_description_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary,
                                focusedContainerColor = SleekSurfaceVariant,
                                unfocusedContainerColor = SleekSurfaceVariant,
                                focusedBorderColor = IndigoDarkAccent,
                                unfocusedBorderColor = SleekDivider,
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekTextSecondary,
                                focusedPlaceholderColor = SleekMutedText,
                                unfocusedPlaceholderColor = SleekMutedText
                            ),
                            placeholder = { Text("E.g., Groceries from Reliance Smart") }
                        )

                        // Scrolling Category ChipGroup
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Category",
                                style = MaterialTheme.typography.labelMedium,
                                color = SleekTextSecondary
                            )

                            if (categories.isEmpty()) {
                                Text(
                                    text = "⚠️ Create a Category in the Categories tab first",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(categories) { cat ->
                                        val isSelected = selectedCategoryId == cat.id
                                        val colorAccent = remember(cat.colorHex) {
                                            try { Color(android.graphics.Color.parseColor(cat.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
                                        }

                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedCategoryId = cat.id
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            label = { Text(cat.name) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = IconMapping.getIcon(cat.iconName),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) Color.White else colorAccent
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = SleekSurfaceVariant,
                                                labelColor = SleekTextSecondary,
                                                selectedContainerColor = IndigoDarkAccent,
                                                selectedLabelColor = Color.White
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = SleekDivider,
                                                selectedBorderColor = PrimaryDark,
                                                borderWidth = 1.dp,
                                                selectedBorderWidth = 1.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Payment mode selection (Cash, Bank, Credit Card)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Mode of Payment",
                                style = MaterialTheme.typography.labelMedium,
                                color = SleekTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Cash", "Bank", "Credit Card").forEach { mode ->
                                    val isSelected = selectedPaymentMode == mode
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) PrimaryDark else SleekDivider,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedPaymentMode = mode
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) IndigoDarkAccent else SleekSurfaceVariant
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mode,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else SleekTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Date Pick Section
                        val dateText = remember(selectedTimestamp) {
                            SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(selectedTimestamp))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekDivider, RoundedCornerShape(12.dp))
                                .clickable {
                                    showDatePicker = true
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date picker",
                                    tint = PrimaryDark
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Change Date",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryDark
                            )
                        }

                        // Native Compose M3 DatePickerDialog
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = selectedTimestamp
                            )
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            selectedTimestamp = datePickerState.selectedDateMillis ?: selectedTimestamp
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text("Confirm", color = IndigoDarkAccent)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDatePicker = false }
                                    ) {
                                        Text("Cancel", color = SleekTextSecondary)
                                    }
                                },
                                colors = DatePickerDefaults.colors(
                                    containerColor = SleekSurface
                                )
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    colors = DatePickerDefaults.colors(
                                        containerColor = SleekSurface,
                                        titleContentColor = Color.White,
                                        headlineContentColor = Color.White,
                                        weekdayContentColor = SleekTextSecondary,
                                        subheadContentColor = SleekTextSecondary,
                                        selectedDayContainerColor = IndigoDarkAccent,
                                        selectedDayContentColor = Color.White,
                                        todayContentColor = PrimaryDark,
                                        todayDateBorderColor = PrimaryDark,
                                        dayContentColor = Color.White
                                    )
                                )
                            }
                        }

                        // Success Indicator Toast Simulation
                        AnimatedVisibility(
                            visible = showExpenseSavedMessage,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Transaction saved successfully! ✓",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Save Button
                        Button(
                            onClick = {
                                val amount = expenseAmount.toDoubleOrNull()
                                if (amount != null && selectedCategoryId != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.addExpense(
                                        amount = amount,
                                        description = expenseDescription,
                                        categoryId = selectedCategoryId!!,
                                        paymentMode = selectedPaymentMode,
                                        timestamp = selectedTimestamp
                                    )
                                    // Reset Fields
                                    expenseAmount = ""
                                    expenseDescription = ""
                                    selectedCategoryId = categories.firstOrNull()?.id
                                    showExpenseSavedMessage = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_expense_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = expenseAmount.isNotEmpty() && selectedCategoryId != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showExpenseSavedMessage) Color(0xFF10B981) else IndigoDarkAccent,
                                contentColor = Color.White,
                                disabledContainerColor = SleekSurfaceVariant,
                                disabledContentColor = SleekMutedText
                            )
                        ) {
                            Icon(
                                imageVector = if (showExpenseSavedMessage) Icons.Default.CheckCircle else Icons.Default.CheckCircle, 
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showExpenseSavedMessage) "Log Saved ✓" else "Save Expense Log",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // --- CATEGORIES LOG TAB ---
        if (activeTab == "Categories") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (editingCategory != null) "Edit Custom Category" else "Create Custom Category",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SleekTextPrimary
                        )

                        // Category Name
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it },
                            label = { Text("Category Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary,
                                focusedContainerColor = SleekSurfaceVariant,
                                unfocusedContainerColor = SleekSurfaceVariant,
                                focusedBorderColor = IndigoDarkAccent,
                                unfocusedBorderColor = SleekDivider,
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekTextSecondary,
                                focusedPlaceholderColor = SleekMutedText,
                                unfocusedPlaceholderColor = SleekMutedText
                            ),
                            placeholder = { Text("E.g., Streaming & Subs") }
                        )

                        // Category Budget Limit
                        OutlinedTextField(
                            value = categoryLimit,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]{0,2}$"))) {
                                    categoryLimit = it
                                }
                            },
                            label = { Text("Monthly Budget Limit (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_limit_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary,
                                focusedContainerColor = SleekSurfaceVariant,
                                unfocusedContainerColor = SleekSurfaceVariant,
                                focusedBorderColor = IndigoDarkAccent,
                                unfocusedBorderColor = SleekDivider,
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekTextSecondary,
                                focusedPlaceholderColor = SleekMutedText,
                                unfocusedPlaceholderColor = SleekMutedText
                            ),
                            placeholder = { Text("3000") }
                        )

                        // Pure swipeable selector for icons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Pick Category Emblem Icon",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Swipe horizontally to see all options",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }

                            // Clean horizontal scrolling container
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SleekSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(supportedIcons) { iconKey ->
                                    val isSelected = selectedIconName == iconKey
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) IndigoDarkAccent else SleekSurface.copy(alpha = 0.8f)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) PrimaryDark else SleekDivider,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedIconName = iconKey
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconMapping.getIcon(iconKey),
                                            contentDescription = iconKey,
                                            tint = if (isSelected) Color.White else SleekTextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Pure swipeable selector for colors
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Choose Visual Theme Color",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Swipe horizontally to see all options",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }

                            // Clean horizontal scrolling container for colors
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SleekSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(paletteColors) { colorString ->
                                    val isSelected = selectedColorHex.lowercase() == colorString.lowercase()
                                    val composeColor = remember { Color(android.graphics.Color.parseColor(colorString)) }
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(composeColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else SleekDivider,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedColorHex = colorString
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected checking icon",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showCategorySavedMessage,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Category changes saved successfully! ✓",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Save Category Button
                        Button(
                            onClick = {
                                val limit = categoryLimit.toDoubleOrNull() ?: 1000.0
                                if (categoryName.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (editingCategory != null) {
                                        viewModel.updateCategory(
                                            id = editingCategory!!.id,
                                            name = categoryName,
                                            iconName = selectedIconName,
                                            colorHex = selectedColorHex,
                                            monthlyLimit = limit
                                        )
                                        editingCategory = null
                                    } else {
                                        viewModel.addCategory(
                                            name = categoryName,
                                            iconName = selectedIconName,
                                            colorHex = selectedColorHex,
                                            monthlyLimit = limit
                                        )
                                    }
                                    // Reset Fields
                                    categoryName = ""
                                    categoryLimit = ""
                                    selectedIconName = "ShoppingCart"
                                    selectedColorHex = "#3B82F6"
                                    showCategorySavedMessage = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_category_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = categoryName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCategorySavedMessage) Color(0xFF10B981) else IndigoDarkAccent,
                                contentColor = Color.White,
                                disabledContainerColor = SleekSurfaceVariant,
                                disabledContentColor = SleekMutedText
                            )
                        ) {
                            Icon(
                                imageVector = if (showCategorySavedMessage) Icons.Default.CheckCircle else Icons.Default.CheckCircle, 
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showCategorySavedMessage) "Category Saved ✓" else (if (editingCategory != null) "Save Customizations" else "Create Category Theme"),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Edit Reset
                        if (editingCategory != null) {
                            OutlinedButton(
                                onClick = {
                                    editingCategory = null
                                    categoryName = ""
                                    categoryLimit = ""
                                    selectedIconName = "ShoppingCart"
                                    selectedColorHex = "#3B82F6"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PrimaryDark
                                ),
                                border = BorderStroke(1.dp, SleekDivider)
                            ) {
                                Text("Discard Edits / Write New")
                            }
                        }
                    }
                }
            }

            // Categories manager list
            item {
                Text(
                    text = "Tap a category to edit or customize",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(categories) { category ->
                var showDeleteConfirmation by remember { mutableStateOf(false) }

                val defaultBgColor = remember(category.colorHex) {
                    try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
                }

                if (showDeleteConfirmation) {
                    // Try to extract a safe fallback category ID that is NOT this category
                    val fallbackCat = categories.firstOrNull { it.id != category.id }
                    
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text("Delete Category: ${category.name}?") },
                        text = {
                            Text(
                                "Choose what to do with associated expenses. " +
                                "You can migrate them to a Miscellaneous category (if available) " +
                                "or completely delete all transactions of Category ${category.name} permanently."
                            )
                        },
                        confirmButton = {
                            if (fallbackCat != null) {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteCategory(category, fallbackCat.id)
                                        showDeleteConfirmation = false
                                        if (editingCategory?.id == category.id) {
                                            editingCategory = null
                                            categoryName = ""
                                            categoryLimit = ""
                                        }
                                    }
                                ) {
                                    Text("Keep Expenses (Move to ${fallbackCat.name})")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteCategory(category, null)
                                    showDeleteConfirmation = false
                                    if (editingCategory?.id == category.id) {
                                        editingCategory = null
                                        categoryName = ""
                                        categoryLimit = ""
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete All (Cascade)")
                            }
                        }
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(12.dp))
                        .clickable {
                            editingCategory = category
                            categoryName = category.name
                            categoryLimit = category.monthlyLimit.toString()
                            selectedIconName = category.iconName
                            selectedColorHex = category.colorHex
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                listState.animateScrollToItem(1)
                            }
                        },
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Left vertical colored action pill
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(defaultBgColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Monthly limit: ${currencyFormatter.format(category.monthlyLimit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    editingCategory = category
                                    categoryName = category.name
                                    categoryLimit = category.monthlyLimit.toString()
                                    selectedIconName = category.iconName
                                    selectedColorHex = category.colorHex
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(1)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit category",
                                    tint = PrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete category",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SETUP LAB ---
        if (activeTab == "Setup & Bills") {
            // Salary Customizer
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Monthly Salary Limit Configuration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekTextPrimary
                        )

                        OutlinedTextField(
                            value = salaryInput,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]{0,2}$"))) {
                                    salaryInput = it
                                }
                            },
                            label = { Text("Your Monthly Salary (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("salary_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary,
                                focusedContainerColor = SleekSurfaceVariant,
                                unfocusedContainerColor = SleekSurfaceVariant,
                                focusedBorderColor = IndigoDarkAccent,
                                unfocusedBorderColor = SleekDivider,
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekTextSecondary,
                                focusedPlaceholderColor = SleekMutedText,
                                unfocusedPlaceholderColor = SleekMutedText
                            ),
                            placeholder = { Text("E.g., 65000") }
                        )

                        Button(
                            onClick = {
                                val value = salaryInput.toDoubleOrNull()
                                if (value != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.updateSalary(value)
                                    showSalarySavedMessage = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = salaryInput.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showSalarySavedMessage) Color(0xFF10B981) else IndigoDarkAccent,
                                contentColor = Color.White,
                                disabledContainerColor = SleekSurfaceVariant,
                                disabledContentColor = SleekMutedText
                            )
                        ) {
                            Icon(
                                imageVector = if (showSalarySavedMessage) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showSalarySavedMessage) "Updated ✓" else "Update",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Recurring bill subscription suite creator
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDivider, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Schedule Recurring Bill / Subscription",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekTextPrimary
                        )

                        OutlinedTextField(
                            value = billName,
                            onValueChange = { billName = it },
                            label = { Text("Subscription / Bill name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary,
                                focusedContainerColor = SleekSurfaceVariant,
                                unfocusedContainerColor = SleekSurfaceVariant,
                                focusedBorderColor = IndigoDarkAccent,
                                unfocusedBorderColor = SleekDivider,
                                focusedLabelColor = PrimaryDark,
                                unfocusedLabelColor = SleekTextSecondary,
                                focusedPlaceholderColor = SleekMutedText,
                                unfocusedPlaceholderColor = SleekMutedText
                            ),
                            placeholder = { Text("E.g. Netflix Premium Plan") }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = billAmount,
                                onValueChange = { 
                                    if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]{0,2}$"))) {
                                        billAmount = it
                                    }
                                },
                                label = { Text("Bill Amount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary,
                                    focusedContainerColor = SleekSurfaceVariant,
                                    unfocusedContainerColor = SleekSurfaceVariant,
                                    focusedBorderColor = IndigoDarkAccent,
                                    unfocusedBorderColor = SleekDivider,
                                    focusedLabelColor = PrimaryDark,
                                    unfocusedLabelColor = SleekTextSecondary,
                                    focusedPlaceholderColor = SleekMutedText,
                                    unfocusedPlaceholderColor = SleekMutedText
                                ),
                                placeholder = { Text("₹ 499") }
                            )

                            OutlinedTextField(
                                value = billDayOfMonth,
                                onValueChange = { 
                                    val num = it.toIntOrNull()
                                    if (it.isEmpty() || (num != null && num in 1..31)) {
                                        billDayOfMonth = it
                                    }
                                },
                                label = { Text("Day of Month") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary,
                                    focusedContainerColor = SleekSurfaceVariant,
                                    unfocusedContainerColor = SleekSurfaceVariant,
                                    focusedBorderColor = IndigoDarkAccent,
                                    unfocusedBorderColor = SleekDivider,
                                    focusedLabelColor = PrimaryDark,
                                    unfocusedLabelColor = SleekTextSecondary,
                                    focusedPlaceholderColor = SleekMutedText,
                                    unfocusedPlaceholderColor = SleekMutedText
                                ),
                                placeholder = { Text("5") }
                            )
                        }

                        // Category scrolling selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Link Bill Category",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { cat ->
                                    val isSelected = billCategoryId == cat.id
                                    val colorAccent = remember(cat.colorHex) {
                                        try { Color(android.graphics.Color.parseColor(cat.colorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            billCategoryId = cat.id
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        label = { Text(cat.name) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = IconMapping.getIcon(cat.iconName),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) Color.White else colorAccent
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = SleekSurfaceVariant,
                                            labelColor = SleekTextSecondary,
                                            selectedContainerColor = IndigoDarkAccent,
                                            selectedLabelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = SleekDivider,
                                            selectedBorderColor = PrimaryDark,
                                            borderWidth = 1.dp,
                                            selectedBorderWidth = 1.dp
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = billAutoDeduct,
                                onCheckedChange = { billAutoDeduct = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = IndigoDarkAccent,
                                    uncheckedColor = SleekTextSecondary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Auto deduct monthly balance",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Deduct balance automatically when date arrives",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val amount = billAmount.toDoubleOrNull()
                                val day = billDayOfMonth.toIntOrNull() ?: 1
                                if (billName.isNotBlank() && amount != null && billCategoryId != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.addRecurringExpense(
                                        name = billName,
                                        amount = amount,
                                        categoryId = billCategoryId!!,
                                        dayOfMonth = day,
                                        isAutoDeduct = billAutoDeduct
                                    )
                                    // Reset Fields
                                    billName = ""
                                    billAmount = ""
                                    billCategoryId = categories.firstOrNull()?.id
                                    billDayOfMonth = "1"
                                    showBillSavedMessage = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = billName.isNotBlank() && billAmount.isNotEmpty() && billCategoryId != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showBillSavedMessage) Color(0xFF10B981) else IndigoDarkAccent,
                                contentColor = Color.White,
                                disabledContainerColor = SleekSurfaceVariant,
                                disabledContentColor = SleekMutedText
                            )
                        ) {
                            Icon(
                                imageVector = if (showBillSavedMessage) Icons.Default.CheckCircle else Icons.Default.Add, 
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showBillSavedMessage) "Subscriber Bill Scheduled ✓" else "Schedule Subscriber Bill",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Recurring subscription list
            item {
                Text(
                    text = "Scheduled Cycles & Subscriptions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recurringExpenses.isEmpty()) {
                item {
                    Text(
                        text = "No recurring billing cycles scheduled yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(recurringExpenses) { bill ->
                    val colorAccent = remember(bill.categoryColorHex) {
                        try { Color(android.graphics.Color.parseColor(bill.categoryColorHex)) } catch (e: Exception) { Color(0xFF818CF8) }
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
                                        .height(32.dp)
                                        .clip(CircleShape)
                                        .background(colorAccent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = bill.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Day ${bill.dayOfMonth} • Auto: ${if (bill.isAutoDeduct) "Yes" else "No"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekTextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currencyFormatter.format(bill.amount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryDark,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        // recreate item to delete safely
                                        val rec = com.example.data.model.RecurringExpense(
                                            id = bill.id,
                                            name = bill.name,
                                            amount = bill.amount,
                                            categoryId = bill.categoryId,
                                            dayOfMonth = bill.dayOfMonth,
                                            isAutoDeduct = bill.isAutoDeduct,
                                            lastProcessedMonth = bill.lastProcessedMonth
                                        )
                                        viewModel.deleteRecurringExpense(rec)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Bill",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
