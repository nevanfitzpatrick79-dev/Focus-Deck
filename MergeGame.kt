package com.example.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.getSymbolSet
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MergeGameUiState

@Composable
fun MergeGameDialog(
    viewModel: MainViewModel
) {
    val gameState by viewModel.mergeGameState.collectAsState()
    val gamState = gameState ?: return
    val gamificationState by viewModel.gamificationState.collectAsState()

    Dialog(onDismissRequest = { viewModel.dismissMergeGame() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!gamState.isActive) {
                    // Game over state
                    GameOverContent(
                        score = gamState.score,
                        highScore = gamificationState.mergeHighScore,
                        onPlayAgain = {
                            viewModel.startMergeGame(gamificationState.currentTheme)
                        },
                        onDismiss = { viewModel.dismissMergeGame() }
                    )
                } else {
                    // Active game
                    GameHeader(
                        score = gamState.score,
                        timeRemaining = gamState.timeRemainingSeconds,
                        onClose = { viewModel.dismissMergeGame() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MergeGrid(
                        state = gamState,
                        symbolSet = getSymbolSet(gamState.themeName),
                        onTap = { viewModel.onMergeTap(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap two matching symbols to merge them",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun GameHeader(score: Int, timeRemaining: Int, onClose: () -> Unit) {
    val timerColor = when {
        timeRemaining <= 30 -> Color(0xFFE53935)
        timeRemaining <= 60 -> Color(0xFFFB8C00)
        else -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("MERGE", fontWeight = FontWeight.Black,
                fontSize = 11.sp, letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary)
            Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val mins = timeRemaining / 60
            val secs = timeRemaining % 60
            Text("%d:%02d".format(mins, secs),
                fontWeight = FontWeight.Black, fontSize = 22.sp, color = timerColor)
            LinearProgressIndicator(
                progress = { timeRemaining / 180f },
                modifier = Modifier.width(80.dp).height(4.dp),
                color = timerColor,
                trackColor = timerColor.copy(alpha = 0.2f)
            )
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close game",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun MergeGrid(
    state: MergeGameUiState,
    symbolSet: com.example.data.MergeSymbolSet,
    onTap: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in 0..3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0..3) {
                    val index = row * 4 + col
                    val tier = state.grid[index]
                    val isSelected = state.selectedIndex == index
                    val justMerged = state.lastMergedIndex == index

                    // Bounce animation on merge
                    val scale by animateFloatAsState(
                        targetValue = if (justMerged) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "cell_scale_$index"
                    )

                    MergeCell(
                        tier = tier,
                        symbol = if (tier >= 0) symbolSet.symbols.getOrNull(tier) ?: "?" else "",
                        isSelected = isSelected,
                        scale = scale,
                        onClick = { onTap(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MergeCell(
    tier: Int,
    symbol: String,
    isSelected: Boolean,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmpty = tier == -1

    // Tier-based background colours
    val bgColor = when {
        isEmpty -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        else -> tierColor(tier)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .background(bgColor, RoundedCornerShape(10.dp))
            .then(
                if (isSelected) Modifier.border(
                    2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)
                ) else Modifier
            )
            .clickable(enabled = !isEmpty) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!isEmpty) {
            Text(
                symbol,
                fontSize = (20 + tier * 2).sp,  // higher tier = slightly larger
                textAlign = TextAlign.Center
            )
        }
    }
}

fun tierColor(tier: Int): androidx.compose.ui.graphics.Color = when (tier) {
    0 -> Color(0xFF37474F).copy(alpha = 0.5f)
    1 -> Color(0xFF1565C0).copy(alpha = 0.4f)
    2 -> Color(0xFF2E7D32).copy(alpha = 0.4f)
    3 -> Color(0xFF6A1B9A).copy(alpha = 0.4f)
    4 -> Color(0xFF00838F).copy(alpha = 0.45f)
    5 -> Color(0xFFE65100).copy(alpha = 0.45f)
    6 -> Color(0xFFC62828).copy(alpha = 0.45f)
    7 -> Color(0xFFAD1457).copy(alpha = 0.5f)
    8 -> Color(0xFFFFD700).copy(alpha = 0.5f)
    else -> Color.Gray.copy(alpha = 0.3f)
}

@Composable
fun GameOverContent(
    score: Int,
    highScore: Int,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val isNewHighScore = score >= highScore && score > 0
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(if (isNewHighScore) "🏆 New High Score!" else "⏱ Time's Up!",
            fontWeight = FontWeight.Black, fontSize = 20.sp,
            color = if (isNewHighScore) Color(0xFFFFD700)
                    else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("$score", fontWeight = FontWeight.Black, fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary)
        Text("points", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        if (!isNewHighScore && highScore > 0) {
            Text("Best: $highScore", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        val dgEarned = (score / 10).coerceAtMost(30)
        if (dgEarned > 0) {
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )) {
                Text("💎 +$dgEarned Dopamine Gold earned",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Done")
            }
            Button(onClick = onPlayAgain, modifier = Modifier.weight(1f)) {
                Text("Play Again")
            }
        }
    }
}
