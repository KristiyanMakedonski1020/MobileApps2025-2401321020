package com.mobileapp2025.expensebudget.data.repository

import com.mobileapp2025.expensebudget.data.local.dao.CategoryDao
import com.mobileapp2025.expensebudget.data.local.dao.ExpenseDao
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Thrown when a category cannot be deleted because expenses still use it.
 */
class CategoryInUseException : Exception("Category is in use")

/**
 * The only way ViewModels access category data.
 */
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun insert(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    /** @throws CategoryInUseException if at least one expense uses this category. */
    suspend fun delete(category: CategoryEntity) {
        if (expenseDao.countByCategory(category.id) > 0) throw CategoryInUseException()
        categoryDao.delete(category)
    }
}