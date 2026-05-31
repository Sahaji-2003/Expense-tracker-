package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBudgetDao {
    @Query("SELECT * FROM user_budgets WHERE id = 1 LIMIT 1")
    fun getUserBudgetFlow(): Flow<UserBudget?>

    @Query("SELECT * FROM user_budgets WHERE id = 1 LIMIT 1")
    suspend fun getUserBudgetDirect(): UserBudget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserBudget(userBudget: UserBudget)
}
