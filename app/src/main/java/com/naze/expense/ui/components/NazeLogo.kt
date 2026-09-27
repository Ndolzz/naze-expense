package com.naze.expense.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.expense.R

/**
 * Simbol brand Naze: huruf "N" geometris yang digambar sebagai satu tarikan
 * garis (single stroke) dengan ujung membulat. Batang kanan menjulur sedikit
 * lebih tinggi = arah & pertumbuhan, tanpa literal bar-chart/wallet.
 * Solid/flat — tidak bergantung pada gradient, tetap jelas di 24-32px.
 */
@Composable
fun NazeSymbol(
    size: Dp = 28.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(R.drawable.ic_naze_symbol),
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size),
    )
}

/**
 * Wordmark "Naze Expense": "Naze" tebal + "Expense" regular,
 * sans-serif sistem yang clean dengan letter-spacing sedikit rapat.
 */
@Composable
fun NazeWordmark(
    fontSize: Int = 22,
    modifier: Modifier = Modifier,
    boldColor: Color = MaterialTheme.colorScheme.onBackground,
    lightColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    androidx.compose.foundation.text.BasicText(
        text = buildString {},
        modifier = modifier,
    )
}

@Composable
fun NazeLogo(
    symbolSize: Dp = 28.dp,
    fontSize: Int = 22,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    onBackground: Color = MaterialTheme.colorScheme.onBackground,
    onBackgroundLight: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NazeSymbol(size = symbolSize, tint = tint)
        WordmarkText(fontSize, onBackground, onBackgroundLight)
    }
}

@Composable
private fun WordmarkText(fontSize: Int, boldColor: Color, lightColor: Color) {
    val bold = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold, color = boldColor)
    val light = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Normal, color = lightColor)
    androidx.compose.foundation.text.BasicText("Naze", style = bold)
    androidx.compose.foundation.text.BasicText(" Expense", style = light)
}
