package com.mobileapp2025.expensebudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobileapp2025.expensebudget.data.local.dao.CategoryDao
import com.mobileapp2025.expensebudget.data.local.dao.ExpenseDao
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity

/**
 * The Room database. Creates the six starter categories on first launch.
 */
@Database(
    entities = [CategoryEntity::class, ExpenseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_budget.db",
                ).addCallback(SeedCallback).build().also { instance = it }
            }
    }
}

/**
 * Inserts the starter categories when the database file is created.
 */
private object SeedCallback : RoomDatabase.Callback() {
    private val starterCategories = listOf(
        "Food" to "#4CAF50",
        "Transport" to "#2196F3",
        "Bills" to "#FF9800",
        "Entertainment" to "#9C27B0",
        "Health" to "#F44336",
        "Other" to "#607D8B",
    )

    override fun onCreate(db: SupportSQLiteDatabase) {
        starterCategories.forEach { (name, color) ->
            db.execSQL(
                "INSERT INTO categories (name, colorHex, monthlyBudgetCents) VALUES (?, ?, NULL)",
                arrayOf<Any>(name, color),
            )
        }
    }
}