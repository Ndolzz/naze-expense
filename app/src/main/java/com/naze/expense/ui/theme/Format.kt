package com.naze.expense.ui.theme

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Format nominal sesuai mata uang terpilih. */
fun formatAmount(amount: Long, currencyCode: String): String {
    val symbols = mapOf(
        "IDR" to "Rp",
        "USD" to "$",
        "EUR" to "\u20AC",
        "SGD" to "S$",
        "MYR" to "RM",
        "JPY" to "\u00A5",
    )
    val symbol = symbols[currencyCode] ?: currencyCode
    val formatter = DecimalFormat("#,###")
    val formatted = formatter.format(kotlin.math.abs(amount))
    return "$symbol$formatted"
}

fun formatSigned(amount: Long, currencyCode: String, isIncome: Boolean): String =
    (if (isIncome) "+" else "-") + formatAmount(amount, currencyCode)

fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(Date(millis))

fun formatDateShort(millis: Long): String =
    SimpleDateFormat("d MMM", Locale("id", "ID")).format(Date(millis))

fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun formatDayHeader(millis: Long): String {
    val today = startOfDay()
    return when (startOfDay(millis)) {
        today -> "Hari ini"
        today - 86_400_000 -> "Kemarin"
        else -> formatDate(millis)
    }
}

/** Awal & akhir bulan berjalan (epoch millis). */
fun currentMonthRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis - 1
    return start to end
}

fun monthLabel(millis: Long = System.currentTimeMillis()): String =
    SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date(millis))
