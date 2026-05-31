package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("""
        SELECT r.id, r.name, r.amount, r.categoryId, r.dayOfMonth, r.isAutoDeduct, r.lastProcessedMonth,
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM recurring_expenses r
        INNER JOIN categories c ON r.categoryId = c.id 
        ORDER BY r.dayOfMonth ASC
    """)
    fun getAllRecurringExpensesWithCategory(): Flow<List<RecurringExpenseWithCategory>>

    @Query("SELECT * FROM recurring_expenses")
    suspend fun getAllRecurringExpensesDirect(): List<RecurringExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense): Long

    @Update
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)

    @Delete
    suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense)
}
