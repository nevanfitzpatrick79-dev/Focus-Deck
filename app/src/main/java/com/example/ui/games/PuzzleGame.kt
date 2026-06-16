package com.example.ui.games

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.getSymbolSet
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PuzzleGameUiState
import com.example.viewmodel.PuzzlePhase

@Composable
fun PuzzleGameDialog(viewModel: MainViewModel) {
    val gameState by viewModel.puzzleGameState.collectAsState()
    val gamificationState by viewModel.gamificationState.collectAsState()
    val state = gameState ?: return

    Dialog(onDismissRequest = { viewModel.dismissPuzzleGame() }) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!state.isActive || state.phase == PuzzlePhase.COMPLETE) {
                    PuzzleGameOver(
                        score = state.score,
                        highLevel = state.level,
                        onPlayAgain = {
                            viewModel.startPuzzleGame(gamificationState.currentTheme)
                        },
                        onDismiss = { viewModel.dismissPuzzleGame() }
                    )
                } else {
                    PuzzleHeader(state = state, onClose = { viewModel.dismissPuzzleGame() })
                    Spacer(modifier = Modifier.height(12.dp))
                    PuzzlePhaseLabel(state.phase)
                    Spacer(modifier = Modifier.height(8.dp))
                    PuzzleGrid(
                        state = state,
                        symbolSet = getSymbolSet(state.themeName),
                        onTap = { viewModel.onPuzzleTap(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val tapped = state.playerGrid.count { it }
                    if (state.phase == PuzzlePhase.RECALLING) {
                        Text(
                            "$tapped / ${state.cellsToShow} selected",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PuzzleHeader(state: PuzzleGameUiState, onClose: () -> Unit) {
    val timerColor = when {
        state.timeRemainingSeconds <= 30 -> Color(0xFFE53935)
        state.timeRemainingSeconds <= 60 -> Color(0xFFFB8C00)
        else -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("PATTERN", fontWeight = FontWeight.Black,
                fontSize = 11.sp, letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary)
            Text("Score: ${state.score}  |  Level ${state.level}",
                fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val mins = state.timeRemainingSeconds / 60
            val secs = state.timeRemainingSeconds % 60
            Text("%d:%02d".format(mins, secs),
                fontWeight = FontWeight.Black, fontSize = 22.sp,
                color = timerColor)
            LinearProgressIndicator(
                progress = { state.timeRemainingSeconds / 180f },
                modifier = Modifier.width(80.dp).height(4.dp),
                color = timerColor,
                trackColor = timerColor.copy(alpha = 0.2f)
            )
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun PuzzlePhaseLabel(phase: PuzzlePhase) {
    val (text, color) = when (phase) {
        PuzzlePhase.SHOWING -> "Memorise the pattern" to MaterialTheme.colorScheme.primary
        PuzzlePhase.RECALLING -> "Now recreate it" to MaterialTheme.colorScheme.secondary
        PuzzlePhase.CORRECT -> "✓ Correct!" to Color(0xFF43A047)
        PuzzlePhase.WRONG -> "Not quite — try again" to Color(0xFFE53935)
        PuzzlePhase.COMPLETE -> "Time's up!" to MaterialTheme.colorScheme.error
    }
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
}

@Composable
fun PuzzleGrid(
    state: PuzzleGameUiState,
    symbolSet: com.example.data.MergeSymbolSet,
    onTap: (Int) -> Unit
) {
    val symbol = symbolSet.symbols.getOrElse(
        (state.level - 1).coerceIn(0, 8)) { "★" }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val isTarget = state.grid[index]
                    val isPlayerSelected = state.playerGrid[index]
                    val isWrong = state.phase == PuzzlePhase.WRONG &&
                        state.playerGrid[index] != state.grid[index]

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            state.phase == PuzzlePhase.SHOWING && isTarget ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            state.phase == PuzzlePhase.CORRECT && isTarget ->
                                Color(0xFF43A047).copy(alpha = 0.6f)
                            state.phase == PuzzlePhase.WRONG && isWrong ->
                                Color(0xFFE53935).copy(alpha = 0.4f)
                            isPlayerSelected ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else ->
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        label = "cell_color_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .then(
                                if (state.phase == PuzzlePhase.RECALLING)
                                    Modifier.clickable { onTap(index) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.phase == PuzzlePhase.SHOWING && isTarget) {
                            Text(symbol, fontSize = 24.sp)
                        } else if (isPlayerSelected &&
                            state.phase == PuzzlePhase.RECALLING) {
                            Text(symbol, fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PuzzleGameOver(
    score: Int,
    highLevel: Int,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text("🧩 Time's Up!", fontWeight = FontWeight.Black,
            fontSize = 20.sp, color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("$score", fontWeight = FontWeight.Black, fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary)
        Text("points", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text("Reached Level $highLevel", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        val dgEarned = (score / 5).coerceAtMost(30)
        if (dgEarned > 0) {
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )) {
                Text("💎 +$dgEarned Dopamine Gold earned",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
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
