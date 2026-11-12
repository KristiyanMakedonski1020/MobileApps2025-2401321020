package com.mobileapp2025.expensebudget.domain

/** Result of parsing a user-typed amount. */
sealed interface AmountResult {
    data class Valid(val cents: Long) : AmountResult

    data class Invalid(val error: AmountError) : AmountResult
}

/** Why an amount was rejected. */
enum class AmountError { EMPTY, NOT_A_NUMBER, NOT_POSITIVE, TOO_MANY_DECIMALS, TOO_LARGE }

/**
 * Converts text such as "12,50" or "12.5" into euro cents without floating point math.
 */
object AmountParser {
    private val numberRegex = Regex("""\d+(\.\d+)?""")
    private const val MAX_INTEGER_DIGITS = 9
    private const val MAX_DECIMALS = 2
    private const val CENTS_PER_UNIT = 100L

    fun parse(input: String): AmountResult {
        val text = input.trim().replace(',', '.')
        if (text.isEmpty()) return AmountResult.Invalid(AmountError.EMPTY)

        val negative = text.startsWith("-")
        val body = text.removePrefix("-")
        if (!numberRegex.matches(body)) return AmountResult.Invalid(AmountError.NOT_A_NUMBER)
        if (negative) return AmountResult.Invalid(AmountError.NOT_POSITIVE)

        val integerPart = body.substringBefore('.')
        val decimals = body.substringAfter('.', "")
        if (decimals.length > MAX_DECIMALS) return AmountResult.Invalid(AmountError.TOO_MANY_DECIMALS)
        if (integerPart.length > MAX_INTEGER_DIGITS) return AmountResult.Invalid(AmountError.TOO_LARGE)

        val cents = integerPart.toLong() * CENTS_PER_UNIT + decimals.padEnd(MAX_DECIMALS, '0').toLong()
        return if (cents <= 0) {
            AmountResult.Invalid(AmountError.NOT_POSITIVE)
        } else {
            AmountResult.Valid(cents)
        }
    }
}