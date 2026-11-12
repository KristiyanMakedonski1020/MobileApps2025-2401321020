package com.mobileapp2025.expensebudget.data.repository

import com.mobileapp2025.expensebudget.data.local.dao.ExpenseDao
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity
import com.mobileapp2025.expensebudget.data.local.model.CategoryTotal
import com.mobileapp2025.expensebudget.data.local.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The only way ViewModels access expense data. Hides the DAO from the UI layer.
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun observeExpenses(from: Long, to: Long): Flow<List<ExpenseWithCategory>> =
        expenseDao.observeExpenses(from, to)

    fun observeTotal(from: Long, to: Long): Flow<Long> =
        expenseDao.observeTotal(from, to).map { it ?: 0L }

    fun observeTotalsByCategory(from: Long, to: Long): Flow<List<CategoryTotal>> =
        expenseDao.observeTotalsByCategory(from, to)

    suspend fun getById(id: Long): ExpenseEntity? = expenseDao.getById(id)

    suspend fun insert(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun update(expense: ExpenseEntity) = expenseDao.update(expense)

    suspend fun delete(expense: ExpenseEntity) = expenseDao.delete(expense)
}