package com.mobileapp2025.expensebudget.domain

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportFormatterTest {

    @Test
    fun `report shows budgets and flags exceeded ones`() {
        val lines = listOf(
            ReportLine("Food", 5_000, 20_000),
            ReportLine("Transport", 3_000, 2_500),
            ReportLine("Other", 1_000, null),
        )

        val report = ReportFormatter.format(YearMonth.of(2026, 11), 12_345, lines)

        val expected = listOf(
            "Expense report - November 2026",
            "Total: €123.45",
            "",
            "- Food: €50.00 / €200.00",
            "- Transport: €30.00 / €25.00 [OVER BUDGET]",
            "- Other: €10.00",
        ).joinToString("\n")
        assertEquals(expected, report)
    }

    @Test
    fun `report with no lines still has header`() {
        val report = ReportFormatter.format(YearMonth.of(2026, 1), 0, emptyList())
        assertEquals("Expense report - January 2026\nTotal: €0.00", report)
    }
}