package com.mobileapp2025.expensebudget.domain

import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MonthRangeTest {

    private val utc = ZoneId.of("UTC")
    private val millisPerDay = 86_400_000L

    @Test
    fun `leap year february has 29 days`() {
        val range = MonthRange.of(YearMonth.of(2024, 2), utc)
        assertEquals(29L, (range.endMillis - range.startMillis) / millisPerDay)
    }

    @Test
    fun `non leap february has 28 days`() {
        val range = MonthRange.of(YearMonth.of(2025, 2), utc)
        assertEquals(28L, (range.endMillis - range.startMillis) / millisPerDay)
    }

    @Test
    fun `december ends where january starts`() {
        val december = MonthRange.of(YearMonth.of(2025, 12), utc)
        val january = MonthRange.of(YearMonth.of(2026, 1), utc)
        assertEquals(january.startMillis, december.endMillis)
    }
}