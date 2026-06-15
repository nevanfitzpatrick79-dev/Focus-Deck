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

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToShop: () -> Unit
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
                    onBreakClick = onNavigateToShop // Go to shop for rewards
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
    }
}
