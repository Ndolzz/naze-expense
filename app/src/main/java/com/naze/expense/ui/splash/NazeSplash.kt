package com.naze.expense.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen "Naze Financial OS".
 * Sengaja dibuat ringan (tanpa aset gambar / video) supaya aplikasi
 * tetap siap dipakai dalam waktu kurang dari 2 detik.
 */
@Composable
fun NazeSplash(onFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.3f) }
    val textAlpha = remember { Animatable(0f) }
    val bar = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo: fade-in cepat lalu pop dengan spring
        launch {
            logoAlpha.animateTo(1f, tween(350))
            logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        // Judul: muncul setelah logo
        launch {
            delay(300)
            textAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        // Progress bar berjalan 1.6 detik lalu lanjut ke aplikasi
        bar.animateTo(1f, tween(1600, easing = LinearEasing))
        onFinished()
    }

    Surface(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.background,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    Modifier
                        .size(96.dp)
                        .alpha(logoAlpha.value)
                        .scale(logoScale.value)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "N",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
                Text(
                    "Naze Financial OS",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .alpha(textAlpha.value),
                )
                Text(
                    "Kelola uang, tetap offline.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .alpha(textAlpha.value),
                )
                LinearProgressIndicator(
                    progress = { bar.value },
                    modifier = Modifier.padding(top = 32.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
            }
        }
    }
}
