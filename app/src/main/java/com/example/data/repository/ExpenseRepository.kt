package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.ExpenseDao
import com.example.data.local.RecurringExpenseDao
import com.example.data.local.UserBudgetDao
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import com.example.data.model.UserBudget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val userBudgetDao: UserBudgetDao,
    private val recurringExpenseDao: RecurringExpenseDao
) {
    val categories: Flow<List<Category>> = categoryDao.getAllCategories()
    
    val allExpensesWithCategory: Flow<List<ExpenseWithCategory>> = 
        expenseDao.getAllExpensesWithCategory()
        
    val recentExpensesWithCategory: Flow<List<ExpenseWithCategory>> = 
        expenseDao.getRecentExpensesWithCategory(5)

    val recurringExpensesWithCategory: Flow<List<RecurringExpenseWithCategory>> =
        recurringExpenseDao.getAllRecurringExpensesWithCategory()

    val userBudgetFlow: Flow<UserBudget?> = userBudgetDao.getUserBudgetFlow()

    suspend fun getUserBudgetDirect(): UserBudget? = userBudgetDao.getUserBudgetDirect()

    suspend fun insertOrUpdateUserBudget(userBudget: UserBudget) {
        userBudgetDao.insertOrUpdateUserBudget(userBudget)
    }

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category, fallbackCategoryId: Long? = null) {
        if (fallbackCategoryId != null && fallbackCategoryId != category.id) {
            // Re-assign all existing expenses to the fallback category before deleting
            expenseDao.reassignCategory(category.id, fallbackCategoryId)
        }
        categoryDao.deleteCategory(category)
    }

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense): Long =
        recurringExpenseDao.insertRecurringExpense(recurringExpense)

    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense) =
        recurringExpenseDao.updateRecurringExpense(recurringExpense)

    suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense) =
        recurringExpenseDao.deleteRecurringExpense(recurringExpense)

    suspend fun getAllRecurringExpensesDirect(): List<RecurringExpense> =
        recurringExpenseDao.getAllRecurringExpensesDirect()

    suspend fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseWithCategory>> =
        expenseDao.getExpensesByCategory(categoryId)

    fun getExpensesInTimeRange(startTime: Long, endTime: Long): Flow<List<ExpenseWithCategory>> =
        expenseDao.getExpensesInTimeRange(startTime, endTime)

    suspend fun seedDefaultsIfEmpty() = withContext(Dispatchers.IO) {
        if (categoryDao.getCategoryCount() > 0) return@withContext

        val defaults = listOf(
            Category(name = "Rent / Flat", iconName = "Home", colorHex = "#3B82F6", monthlyLimit = 9000.0),
            Category(name = "Education Loan EMI", iconName = "Star", colorHex = "#EF4444", monthlyLimit = 10000.0),
            Category(name = "Home Support", iconName = "Favorite", colorHex = "#6366F1", monthlyLimit = 3000.0),
            Category(name = "Recharges & Utilities", iconName = "Check", colorHex = "#F97316", monthlyLimit = 1200.0),
            Category(name = "Groceries & Cooking", iconName = "ShoppingCart", colorHex = "#0D9488", monthlyLimit = 3000.0),
            Category(name = "Travel & Commute", iconName = "PlayArrow", colorHex = "#06B6D4", monthlyLimit = 2000.0),
            Category(name = "Gym & Supplements", iconName = "Info", colorHex = "#A855F7", monthlyLimit = 4000.0),
            Category(name = "Office Meals & Outings", iconName = "Edit", colorHex = "#10B981", monthlyLimit = 4000.0),
            Category(name = "Miscellaneous", iconName = "Add", colorHex = "#64748B", monthlyLimit = 3000.0)
        )

        val insertedIds = mutableListOf<Long>()
        for (category in defaults) {
            val id = categoryDao.insertCategory(category)
            insertedIds.add(id)
        }

        userBudgetDao.insertOrUpdateUserBudget(UserBudget(id = 1, monthlySalary = 65000.0))

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        if (insertedIds.size >= 9) {
            val rentId = insertedIds[0]
            val loanId = insertedIds[1]
            val homeId = insertedIds[2]
            val utilId = insertedIds[3]
            val grocId = insertedIds[4]
            val travId = insertedIds[5]
            val gymId = insertedIds[6]
            val mealId = insertedIds[7]
            val miscId = insertedIds[8]

            val initialExpenses = listOf(
                Expense(amount = 9000.0, description = "Month Rent Payment", timestamp = now - 5 * oneDay, categoryId = rentId, paymentMode = "Bank"),
                Expense(amount = 10000.0, description = "Education Loan EMI", timestamp = now - 15 * oneDay, categoryId = loanId, paymentMode = "Bank"),
                Expense(amount = 1800.0, description = "Home Support allowance", timestamp = now - 3 * oneDay, categoryId = homeId, paymentMode = "Bank"),
                Expense(amount = 450.0, description = "Jio Fiber ISP bill", timestamp = now - 2 * oneDay, categoryId = utilId, paymentMode = "Credit Card"),
                Expense(amount = 1250.0, description = "D-Mart Grocery run", timestamp = now - 1 * oneDay, categoryId = grocId, paymentMode = "Cash"),
                Expense(amount = 600.0, description = "Fresh vegetables - Zepto", timestamp = now - 4 * oneDay, categoryId = grocId, paymentMode = "Credit Card"),
                Expense(amount = 350.0, description = "Cab commute to work", timestamp = now - 1 * oneDay, categoryId = travId, paymentMode = "Credit Card"),
                Expense(amount = 250.0, description = "Metro card topup", timestamp = now - 6 * oneDay, categoryId = travId, paymentMode = "Cash"),
                Expense(amount = 1800.0, description = "Fitness supplements", timestamp = now - 10 * oneDay, categoryId = gymId, paymentMode = "Credit Card"),
                Expense(amount = 750.0, description = "Office cafeteria lunch", timestamp = now - 2 * oneDay, categoryId = mealId, paymentMode = "Credit Card"),
                Expense(amount = 120.0, description = "Filter coffee snacks", timestamp = now, categoryId = mealId, paymentMode = "Cash"),
                Expense(amount = 450.0, description = "Haircut and saloon", timestamp = now - 8 * oneDay, categoryId = miscId, paymentMode = "Cash")
            )

            for (exp in initialExpenses) {
                expenseDao.insertExpense(exp)
            }

            val recurringExpenses = listOf(
                RecurringExpense(name = "Netflix Standard HD Plan", amount = 499.0, categoryId = miscId, dayOfMonth = 5, isAutoDeduct = true),
                RecurringExpense(name = "House Rent Helper", amount = 2500.0, categoryId = homeId, dayOfMonth = 1, isAutoDeduct = false),
                RecurringExpense(name = "Broadband Wi-Fi Connection", amount = 707.0, categoryId = utilId, dayOfMonth = 10, isAutoDeduct = true)
            )

            for (rec in recurringExpenses) {
                recurringExpenseDao.insertRecurringExpense(rec)
            }
        }
    }
}
