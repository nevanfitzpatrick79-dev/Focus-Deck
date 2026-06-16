package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import com.example.ui.components.CustomSnackbarOverlay
import com.example.ui.components.FocusTimer
import com.example.ui.components.TaskBoard
import com.example.ui.components.ThemeBackgroundLayer
import com.example.ui.components.TopDopamineBar
import com.example.ui.components.WorkingMemoryAnchor
import com.example.viewmodel.MainViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToShop: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBadges: () -> Unit
) {
    val gamificationState by viewModel.gamificationState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val timerState by viewModel.focusTimerState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val workingMemory = gamificationState.workingMemoryAnchor
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        val showConfetti by viewModel.showConfetti.collectAsState()

        ThemeBackgroundLayer(themeName = gamificationState.currentTheme)

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopDopamineBar(
                    state = gamificationState,
                    onBreakClick = onNavigateToShop, // Go to shop for rewards
                    onSettingsClick = onNavigateToSettings,
                    onBadgesClick = onNavigateToBadges
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                
                WorkingMemoryAnchor(
                    text = workingMemory,
                    onTextChanged = viewModel::updateWorkingMemory,
                    name = gamificationState.userName
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Daily summary row
                val completedToday = tasks.count { task ->
                    task.isCompleted &&
                    (System.currentTimeMillis() - task.timestamp) < 86_400_000L
                }
                val activeCount = tasks.count { !it.isCompleted }

                if (gamificationState.isProfileSetup) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Streak chip
                        if (gamificationState.dailyFlowStreak > 0) {
                            SummaryChip(
                                "🔥 ${gamificationState.dailyFlowStreak}d streak",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Tasks completed today
                        SummaryChip(
                            "✅ $completedToday done",
                            modifier = Modifier.weight(1f)
                        )
                        // Active tasks
                        if (activeCount > 0) {
                            SummaryChip(
                                "📋 $activeCount left",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Tip card
                    com.example.ui.components.DailyTipCard(
                        state = gamificationState,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                
                // Bento Layout equivalent
                FocusTimer(
                    state = timerState,
                    preferredMinutes = gamificationState.preferredFocusMinutes,
                    onStart = viewModel::startTimer,
                    onStop = viewModel::stopTimer
                )
                
                TaskBoard(
                    tasks = tasks,
                    selectedCategory = selectedCategory,
                    enabledCategories = gamificationState.enabledCategories,
                    onCategorySelected = viewModel::setCategory,
                    onAddTask = { title, priority, category, dueDate ->
                        viewModel.addTask(title, priority, category, dueDate)
                    },
                    onToggleTask = viewModel::toggleTask,
                    onDeleteTask = viewModel::deleteTask,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        CustomSnackbarOverlay(message = snackbarMessage)
        com.example.ui.components.ConfettiOverlay(show = showConfetti)
        
        val newBadge by viewModel.newlyEarnedBadge.collectAsState()
        com.example.ui.components.BadgeEarnedOverlay(
            badgeId = newBadge,
            onDismiss = { viewModel.dismissBadge() }
        )
    }
}

@Composable
fun SummaryChip(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}
