package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun ConfettiOverlay(show: Boolean) {
    if (!show) return
    
    var animate by remember { mutableStateOf(false) }
    
    LaunchedEffect(show) {
        animate = false
        kotlinx.coroutines.delay(10)
        animate = true
    }

    val progress by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
        label = "Confetti Progress"
    )

    val particles = remember {
        List(100) {
            val startX = Random.nextFloat()
            val speedX = (Random.nextFloat() - 0.5f) * 0.5f // spread
            val speedY = Random.nextFloat() * 1.5f + 0.5f
            val color = listOf(
                Color(0xFFFFC107), Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFF4CAF50)
            ).random()
            
            ConfettiParticle(startX, speedX, speedY, color)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val curY = -0.1f + progress * p.speedY
            val curX = p.startX + progress * p.speedX
            if (curY < 1.1f) {
                drawCircle(
                    color = p.color,
                    radius = 12f,
                    center = Offset(curX * size.width, curY * size.height)
                )
            }
        }
    }
}

data class ConfettiParticle(
    val startX: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color
)
