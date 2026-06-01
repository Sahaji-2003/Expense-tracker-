package com.example.data.local

import androidx.room.*
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("""
        SELECT r.id, r.name, r.amount, r.categoryId, r.dayOfMonth, r.isAutoDeduct, r.lastProcessedMonth, r.userId,
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM recurring_expenses r
        INNER JOIN categories c ON r.categoryId = c.id 
        WHERE r.userId = :userId
        ORDER BY r.dayOfMonth ASC
    """)
    fun getAllRecurringExpensesWithCategory(userId: String): Flow<List<RecurringExpenseWithCategory>>

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId")
    suspend fun getAllRecurringExpensesDirect(userId: String): List<RecurringExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense): Long

    @Update
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)

    @Delete
    suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense)
}
