package com.mobileapp2025.expensebudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity
import com.mobileapp2025.expensebudget.data.local.model.CategoryTotal
import com.mobileapp2025.expensebudget.data.local.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data access for the "expenses" table.
 * Period queries use the half-open range [from, to).
 */
@Dao
interface ExpenseDao {

    @Transaction
    @Query("SELECT * FROM expenses WHERE date >= :from AND date < :to ORDER BY date DESC, id DESC")
    fun observeExpenses(from: Long, to: Long): Flow<List<ExpenseWithCategory>>

    @Query("SELECT SUM(amountCents) FROM expenses WHERE date >= :from AND date < :to")
    fun observeTotal(from: Long, to: Long): Flow<Long?>

    @Query(
        "SELECT categoryId, SUM(amountCents) AS totalCents FROM expenses " +
                "WHERE date >= :from AND date < :to GROUP BY categoryId",
    )
    fun observeTotalsByCategory(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}