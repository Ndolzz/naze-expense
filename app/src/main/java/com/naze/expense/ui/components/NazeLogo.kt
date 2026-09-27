package com.naze.expense.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.expense.R

/**
 * Simbol brand Naze: huruf "N" geometris yang digambar sebagai satu tarikan
 * garis dengan ujung membulat. Batang kanan menjulur sedikit lebih tinggi
 * (arah & pertumbuhan) — solid/flat, tetap jelas di ukuran 24-32px.
 * Tersedia versi monochrome lewat tint (putih untuk dark bg).
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
 * Wordmark "Naze Expense": "Naze" semi-bold + "Expense" regular,
 * sans-serif sistem yang clean, letter-spacing sedikit rapat.
 */
@Composable
fun NazeWordmark(
    fontSize: Int = 22,
    boldColor: Color = MaterialTheme.colorScheme.onBackground,
    lightColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Naze",
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            color = boldColor,
        )
        Text(
            " Expense",
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.2).sp,
            color = lightColor,
        )
    }
}

/** Logo utama: symbol + wordmark dalam satu baris. */
@Composable
fun NazeLogo(
    symbolSize: Dp = 28.dp,
    fontSize: Int = 22,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    boldColor: Color = MaterialTheme.colorScheme.onBackground,
    lightColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NazeSymbol(size = symbolSize, tint = tint)
        NazeWordmark(fontSize = fontSize, boldColor = boldColor, lightColor = lightColor)
    }
}
