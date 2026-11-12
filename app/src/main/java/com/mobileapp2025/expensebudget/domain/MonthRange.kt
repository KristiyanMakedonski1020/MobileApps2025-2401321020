package com.mobileapp2025.expensebudget.domain

import java.time.YearMonth
import java.time.ZoneId

/**
 * The half-open time range [startMillis, endMillis) that covers one calendar month.
 */
data class MonthRange(val startMillis: Long, val endMillis: Long) {

    companion object {
        fun of(month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): MonthRange {
            val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return MonthRange(start, end)
        }
    }
}