package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import com.example.data.model.UserBudget
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
            checkAndProcessRecurringExpenses()
        }
    }

    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allExpenses: StateFlow<List<ExpenseWithCategory>> = repository.allExpensesWithCategory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentExpenses: StateFlow<List<ExpenseWithCategory>> = repository.recentExpensesWithCategory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recurringExpenses: StateFlow<List<RecurringExpenseWithCategory>> = repository.recurringExpensesWithCategory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userBudget: StateFlow<UserBudget> = repository.userBudgetFlow
        .map { it ?: UserBudget(id = 1, monthlySalary = 65000.0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserBudget(id = 1, monthlySalary = 65000.0)
        )

    private val _timeFrame = MutableStateFlow("This Month")
    val timeFrame: StateFlow<String> = _timeFrame.asStateFlow()

    private val _selectedAnalyticsCategoryId = MutableStateFlow<Long?>(null)
    val selectedAnalyticsCategoryId: StateFlow<Long?> = _selectedAnalyticsCategoryId.asStateFlow()

    fun setTimeFrame(frame: String) {
        _timeFrame.value = frame
        _selectedAnalyticsCategoryId.value = null // Reset selection on timeframe change
    }

    fun setSelectedAnalyticsCategory(categoryId: Long?) {
        _selectedAnalyticsCategoryId.value = categoryId
    }

    fun updateSalary(newSalary: Double) {
        viewModelScope.launch {
            repository.insertOrUpdateUserBudget(UserBudget(id = 1, monthlySalary = newSalary))
        }
    }

    fun addExpense(amount: Double, description: String, categoryId: Long, paymentMode: String, timestamp: Long) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                description = description,
                timestamp = timestamp,
                categoryId = categoryId,
                paymentMode = paymentMode
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                monthlyLimit = monthlyLimit
            )
            repository.insertCategory(category)
        }
    }

    fun updateCategory(id: Long, name: String, iconName: String, colorHex: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val category = Category(
                id = id,
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                monthlyLimit = monthlyLimit
            )
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category, fallbackCategoryId: Long? = null) {
        viewModelScope.launch {
            repository.deleteCategory(category, fallbackCategoryId)
            if (_selectedAnalyticsCategoryId.value == category.id) {
                _selectedAnalyticsCategoryId.value = null
            }
        }
    }

    fun addRecurringExpense(name: String, amount: Double, categoryId: Long, dayOfMonth: Int, isAutoDeduct: Boolean) {
        viewModelScope.launch {
            val recurring = RecurringExpense(
                name = name,
                amount = amount,
                categoryId = categoryId,
                dayOfMonth = dayOfMonth,
                isAutoDeduct = isAutoDeduct
            )
            repository.insertRecurringExpense(recurring)
        }
    }

    fun deleteRecurringExpense(recurring: RecurringExpense) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(recurring)
        }
    }

    private suspend fun checkAndProcessRecurringExpenses() = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val currentYearMonth = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val recurringList = repository.getAllRecurringExpensesDirect()
        for (rec in recurringList) {
            if (rec.dayOfMonth <= currentDay && rec.lastProcessedMonth != currentYearMonth) {
                repository.insertExpense(
                    Expense(
                        amount = rec.amount,
                        description = "Recurring: ${rec.name}",
                        timestamp = System.currentTimeMillis(),
                        categoryId = rec.categoryId,
                        paymentMode = if (rec.isAutoDeduct) "Bank" else "Cash"
                    )
                )
                repository.updateRecurringExpense(
                    rec.copy(lastProcessedMonth = currentYearMonth)
                )
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                val database = AppDatabase.getDatabase(application)
                val repository = ExpenseRepository(
                    database.categoryDao(),
                    database.expenseDao(),
                    database.userBudgetDao(),
                    database.recurringExpenseDao()
                )
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
