package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AvailableBadges
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.gamificationState.collectAsState()
    val earned = state.earnedBadgeIds
    val earnedCount = AvailableBadges.count { earned.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Badges")
                        Text(
                            "$earnedCount / ${AvailableBadges.size} earned",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(AvailableBadges) { badge ->
                val isEarned = earned.contains(badge.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isEarned) 1f else 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEarned)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (isEarned) badge.emoji else "\uD83D\uDD12",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            badge.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isEarned) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                badge.description,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
