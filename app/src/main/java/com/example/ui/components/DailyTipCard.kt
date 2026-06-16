package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamificationState
import com.example.data.Tip
import com.example.data.TipsEngine

@Composable
fun DailyTipCard(
    state: GamificationState,
    modifier: Modifier = Modifier
) {
    val allTips = remember(state.adhdPresentation, state.coOccurring,
        state.peakEnergyTime, state.takesMedication) {
        TipsEngine.getTipsForProfile(state)
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentTip = allTips.getOrElse(currentIndex) {
        TipsEngine.getDailyTip(state)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "💡 Today's Tip",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                IconButton(
                    onClick = {
                        currentIndex = (currentIndex + 1) % allTips.size
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Next tip",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = currentTip,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tip_content"
            ) { tip ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(tip.emoji, fontSize = 20.sp)
                    Text(
                        tip.text,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Tips are general suggestions, not medical advice.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
