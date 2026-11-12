package com.mobileapp2025.expensebudget

import android.app.Application

/**
 * Application entry point. Holds the dependency container.
 */
class BudgetApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}