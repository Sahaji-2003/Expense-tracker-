package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
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
import com.example.data.model.User
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerStatus = MutableStateFlow<String?>(null)
    val registerStatus: StateFlow<String?> = _registerStatus.asStateFlow()

    private val _isSessionLoading = MutableStateFlow(true)
    val isSessionLoading: StateFlow<Boolean> = _isSessionLoading.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        val savedUsername = sharedPrefs.getString("logged_in_username", null)
        if (savedUsername != null) {
            viewModelScope.launch {
                try {
                    val user = repository.getUserByUsername(savedUsername)
                    if (user != null) {
                        _loggedInUser.value = user
                        repository.seedDefaultsIfEmpty(user.username)
                        pruneOldData(user.username)
                        checkAndProcessRecurringExpenses(user.username)
                    } else {
                        sharedPrefs.edit().remove("logged_in_username").apply()
                    }
                } catch (e: Exception) {
                    // Fail-safe fallback
                } finally {
                    _isSessionLoading.value = false
                }
            }
        } else {
            _isSessionLoading.value = false
        }
    }

    val categories: StateFlow<List<Category>> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getCategories(user.username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allExpenses: StateFlow<List<ExpenseWithCategory>> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getAllExpenses(user.username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun getStartOfCurrentMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    val recentExpenses: StateFlow<List<ExpenseWithCategory>> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getRecentExpensesSince(user.username, getStartOfCurrentMonth(), 10)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recurringExpenses: StateFlow<List<RecurringExpenseWithCategory>> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getRecurringExpenses(user.username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userBudget: StateFlow<UserBudget> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(null)
            else repository.getUserBudgetFlow(user.username)
        }
        .map { budget ->
            budget ?: UserBudget(userId = loggedInUser.value?.username ?: "guest", monthlySalary = loggedInUser.value?.monthlySalary ?: 0.0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserBudget(userId = "guest", monthlySalary = 0.0)
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

    suspend fun getUserSessionDirect(username: String): User? {
        return repository.getUserByUsername(username)
    }

    // AUTH ACTIONS
    fun login(username: String, pin: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (username.isBlank() || pin.isBlank()) {
                _loginError.value = "Username and PIN cannot be empty"
                return@launch
            }
            val user = repository.getUserByUsername(username.trim())
            if (user == null) {
                _loginError.value = "User not found. Try registering!"
            } else if (user.pin != pin) {
                _loginError.value = "Incorrect PIN. Please try again."
            } else {
                _loggedInUser.value = user
                sharedPrefs.edit().putString("logged_in_username", user.username).apply()
                repository.seedDefaultsIfEmpty(user.username)
                pruneOldData(user.username)
                checkAndProcessRecurringExpenses(user.username)
            }
        }
    }

    fun register(username: String, pin: String, name: String, monthlySalary: Double) {
        viewModelScope.launch {
            _registerStatus.value = null
            if (username.isBlank() || pin.isBlank() || name.isBlank()) {
                _registerStatus.value = "All fields are required"
                return@launch
            }
            val existing = repository.getUserByUsername(username.trim())
            if (existing != null) {
                _registerStatus.value = "Username already exists"
                return@launch
            }
            
            val newUser = User(
                username = username.trim(),
                name = name.trim(),
                pin = pin,
                monthlySalary = monthlySalary
            )
            repository.insertUser(newUser)
            repository.insertOrUpdateUserBudget(UserBudget(userId = newUser.username, monthlySalary = monthlySalary))
            
            _registerStatus.value = "Success"
            repository.seedDefaultsIfEmpty(newUser.username)
            // Auto log them in
            _loggedInUser.value = newUser
            sharedPrefs.edit().putString("logged_in_username", newUser.username).apply()
            pruneOldData(newUser.username)
        }
    }

    fun logout() {
        _loggedInUser.value = null
        sharedPrefs.edit().remove("logged_in_username").apply()
        _loginError.value = null
        _registerStatus.value = null
    }

    fun clearAuthStatus() {
        _loginError.value = null
        _registerStatus.value = null
    }

    // BUDGET UTILS
    fun updateSalary(newSalary: Double) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(monthlySalary = newSalary)
            repository.updateUser(updatedUser)
            _loggedInUser.value = updatedUser
            repository.insertOrUpdateUserBudget(UserBudget(userId = user.username, monthlySalary = newSalary))
        }
    }

    fun addExpense(amount: Double, description: String, categoryId: Long, paymentMode: String, timestamp: Long) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                description = description,
                timestamp = timestamp,
                categoryId = categoryId,
                paymentMode = paymentMode,
                userId = user.username
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
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            val category = Category(
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                monthlyLimit = monthlyLimit,
                userId = user.username
            )
            repository.insertCategory(category)
        }
    }

    fun updateCategory(id: Long, name: String, iconName: String, colorHex: String, monthlyLimit: Double) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            val category = Category(
                id = id,
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                monthlyLimit = monthlyLimit,
                userId = user.username
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
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            val recurring = RecurringExpense(
                name = name,
                amount = amount,
                categoryId = categoryId,
                dayOfMonth = dayOfMonth,
                isAutoDeduct = isAutoDeduct,
                userId = user.username
            )
            repository.insertRecurringExpense(recurring)
        }
    }

    fun deleteRecurringExpense(recurring: RecurringExpense) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(recurring)
        }
    }

    private suspend fun checkAndProcessRecurringExpenses(userId: String) = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val currentYearMonth = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val recurringList = repository.getAllRecurringExpensesDirect(userId)
        for (rec in recurringList) {
            if (rec.dayOfMonth <= currentDay && rec.lastProcessedMonth != currentYearMonth) {
                repository.insertExpense(
                    Expense(
                        amount = rec.amount,
                        description = "Recurring: ${rec.name}",
                        timestamp = System.currentTimeMillis(),
                        categoryId = rec.categoryId,
                        paymentMode = if (rec.isAutoDeduct) "Bank" else "Cash",
                        userId = userId
                    )
                )
                repository.updateRecurringExpense(
                    rec.copy(lastProcessedMonth = currentYearMonth)
                )
            }
        }
    }

    fun pruneOldData(userId: String) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -3)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val threshold = calendar.timeInMillis
            repository.pruneExpensesOlderThan(userId, threshold)
        }
    }

    // Optional debug seeder for guest or demo logins
    fun seedDemoDefaults() {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty(user.username)
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
                    database.recurringExpenseDao(),
                    database.userDao()
                )
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
