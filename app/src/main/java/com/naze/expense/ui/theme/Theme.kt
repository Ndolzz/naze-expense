package com.naze.expense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.naze.expense.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Primary,
    primaryContainer = SurfaceLight,
    secondary = Income,
    secondaryContainer = IncomeContainer,
    tertiary = Expense,
    tertiaryContainer = ExpenseContainer,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFE7E8F2),
    background = SurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    primaryContainer = Color(0xFF1D2A4D),
    secondary = IncomeContainer,
    secondaryContainer = Color(0xFF1B3824),
    tertiary = ExpenseContainer,
    tertiaryContainer = Color(0xFF4A1F1E),
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF1E2127),
    background = SurfaceDark,
)

private val NazeTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun NazeExpenseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NazeTypography,
        content = content,
    )
}
