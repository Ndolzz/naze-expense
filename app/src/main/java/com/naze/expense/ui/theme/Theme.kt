package com.naze.expense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.expense.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = BrandBlueDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF0E2A5C),
    secondary = Income,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF0B3B1F),
    tertiary = Expense,
    tertiaryContainer = Color(0xFFFEE2E2),
    onTertiaryContainer = Color(0xFF5C1A1A),
    surface = Color(0xFFFBFCFF),
    onSurface = Color(0xFF161B26),
    surfaceVariant = Color(0xFFE9EEF9),
    onSurfaceVariant = Color(0xFF4A5568),
    background = SurfaceLight,
    onBackground = Color(0xFF161B26),
)

private val DarkColors = darkColorScheme(
    primary = BrandBlueBright,
    onPrimary = Color(0xFF0A1830),
    primaryContainer = Color(0xFF1C2D4F),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF06281A),
    secondaryContainer = Color(0xFF103525),
    onSecondaryContainer = Color(0xFFB7F4DC),
    tertiary = Color(0xFFF87171),
    onTertiary = Color(0xFF3B0D0D),
    tertiaryContainer = Color(0xFF3B1D1C),
    onTertiaryContainer = Color(0xFFFECACA),
    surface = Color(0xFF101624),      // kartu & bottom bar
    onSurface = Color(0xFFF2F5FA),     // off-white
    surfaceVariant = Color(0xFF1A2234), // sheet "Lainnya" & kartu sekunder
    onSurfaceVariant = Color(0xFF9AA7BD),
    background = SurfaceDark,         // near-black dark navy
    onBackground = Color(0xFFF2F5FA),
)

private val NazeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val NazeTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
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
        shapes = NazeShapes,
        content = content,
    )
}
