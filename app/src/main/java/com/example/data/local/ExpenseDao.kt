package com.example.data.local

import androidx.room.*
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("""
        SELECT e.id, e.amount, e.description, e.timestamp, e.categoryId, e.paymentMode, 
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.id 
        WHERE e.userId = :userId
        ORDER BY e.timestamp DESC
    """)
    fun getAllExpensesWithCategory(userId: String): Flow<List<ExpenseWithCategory>>

    @Query("""
        SELECT e.id, e.amount, e.description, e.timestamp, e.categoryId, e.paymentMode, 
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.id 
        WHERE e.userId = :userId
        ORDER BY e.timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentExpensesWithCategory(userId: String, limit: Int): Flow<List<ExpenseWithCategory>>

    @Query("""
        SELECT e.id, e.amount, e.description, e.timestamp, e.categoryId, e.paymentMode, 
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.id 
        WHERE e.userId = :userId AND e.timestamp >= :sinceTime
        ORDER BY e.timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentExpensesWithCategorySince(userId: String, sinceTime: Long, limit: Int): Flow<List<ExpenseWithCategory>>

    @Query("""
        SELECT e.id, e.amount, e.description, e.timestamp, e.categoryId, e.paymentMode, 
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.id 
        WHERE e.categoryId = :categoryId AND e.userId = :userId
        ORDER BY e.timestamp DESC
    """)
    fun getExpensesByCategory(userId: String, categoryId: Long): Flow<List<ExpenseWithCategory>>

    @Query("""
        SELECT e.id, e.amount, e.description, e.timestamp, e.categoryId, e.paymentMode, 
               c.name AS categoryName, c.iconName AS categoryIconName, c.colorHex AS categoryColorHex 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.id 
        WHERE e.userId = :userId AND e.timestamp >= :startTime AND e.timestamp <= :endTime
        ORDER BY e.timestamp DESC
    """)
    fun getExpensesInTimeRange(userId: String, startTime: Long, endTime: Long): Flow<List<ExpenseWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("UPDATE expenses SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)

    @Query("DELETE FROM expenses WHERE userId = :userId AND timestamp < :thresholdTime")
    suspend fun pruneExpensesOlderThan(userId: String, thresholdTime: Long)
}
