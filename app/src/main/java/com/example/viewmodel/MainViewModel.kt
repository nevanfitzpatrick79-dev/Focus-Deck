package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.GamificationRepository
import com.example.data.Task
import com.example.data.TaskRepository
import com.example.data.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "focus-deck-db"
    ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
     .build()

    private val taskRepository = TaskRepository(db.taskDao())
    private val gamificationRepository = GamificationRepository(application.dataStore)

    val selectedCategory = MutableStateFlow(com.example.data.TaskCategory.ALL)

    fun setCategory(category: com.example.data.TaskCategory) {
        selectedCategory.value = category
    }

    val tasks: StateFlow<List<Task>> = selectedCategory.flatMapLatest { cat ->
        if (cat == com.example.data.TaskCategory.ALL) {
            taskRepository.allTasks
        } else {
            taskRepository.getByCategory(cat.name)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val gamificationState = gamificationRepository.stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.data.GamificationState(isLoading = false)
    )
    
    private var lastLevel = 1
    
    init {
        viewModelScope.launch {
            gamificationRepository.recordActivityForToday()
        }
        viewModelScope.launch {
            gamificationState.collect { state ->
                if (state.level > lastLevel && lastLevel != 1) {
                    showConfetti.value = true
                    delay(3000)
                    showConfetti.value = false
                }
                lastLevel = state.level
            }
        }
    }

    val focusTimerState = MutableStateFlow(TimerState())
    
    val mergeGameState = MutableStateFlow<MergeGameUiState?>(null)
    private var mergeTimerJob: kotlinx.coroutines.Job? = null

    fun startMergeGame(themeName: String) {
        viewModelScope.launch {
            // Check for saved state in same theme
            val saved = gamificationRepository.loadMergeState()
            val state = if (saved != null && saved.themeName == themeName) {
                MergeGameUiState.fromSave(saved)
            } else {
                MergeGameUiState.newGame(themeName)
            }
            mergeGameState.value = state
            startMergeTimer()
        }
    }

    private fun startMergeTimer() {
        mergeTimerJob?.cancel()
        mergeTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = mergeGameState.value ?: break
                if (!current.isActive) break
                val newTime = current.timeRemainingSeconds - 1
                if (newTime <= 0) {
                    endMergeGame()
                    break
                } else {
                    val updated = current.copy(timeRemainingSeconds = newTime)
                    mergeGameState.value = updated
                    // Auto-save every 10 seconds
                    if (newTime % 10 == 0) {
                        gamificationRepository.saveMergeState(
                            updated.grid, updated.score, newTime, updated.themeName)
                    }
                }
            }
        }
    }

    fun onMergeTap(index: Int) {
        val current = mergeGameState.value ?: return
        if (!current.isActive || current.grid[index] == -1) return

        val selectedIndex = current.selectedIndex
        if (selectedIndex == null) {
            // First tap — select this cell
            mergeGameState.value = current.copy(selectedIndex = index)
        } else if (selectedIndex == index) {
            // Tap same cell — deselect
            mergeGameState.value = current.copy(selectedIndex = null)
        } else if (current.grid[selectedIndex] == current.grid[index]) {
            // Matching tiers — merge!
            val tier = current.grid[index]
            val newTier = if (tier < 8) tier + 1 else tier
            val newGrid = current.grid.toMutableList()
            newGrid[selectedIndex] = newTier
            newGrid[index] = -1
            // Spawn a random low-tier cell in an empty spot
            val emptyIndices = newGrid.indices.filter { newGrid[it] == -1 }
            if (emptyIndices.isNotEmpty()) {
                newGrid[emptyIndices.random()] = (0..1).random()
            }
            val pointsEarned = newTier + 1  // tier 0 merge = 1pt, tier 8 = 9pts
            mergeGameState.value = current.copy(
                grid = newGrid,
                score = current.score + pointsEarned,
                selectedIndex = null,
                lastMergedIndex = selectedIndex,
                lastMergeTier = newTier
            )
        } else {
            // Different tiers — switch selection
            mergeGameState.value = current.copy(selectedIndex = index)
        }
    }

    fun endMergeGame() {
        mergeTimerJob?.cancel()
        val current = mergeGameState.value ?: return
        val finalScore = current.score
        mergeGameState.value = current.copy(isActive = false, timeRemainingSeconds = 0)
        viewModelScope.launch {
            gamificationRepository.clearMergeState()
            gamificationRepository.updateMergeHighScore(finalScore)
            // DG reward: 1 DG per 10 points, capped at 30
            val dgReward = (finalScore / 10).coerceAtMost(30)
            val xpReward = (finalScore / 5).coerceAtMost(50)
            if (dgReward > 0 || xpReward > 0) {
                gamificationRepository.addReward(xpReward, dgReward)
                showSnackbar("🎮 Score: $finalScore — +$xpReward XP, +$dgReward DG")
            }
        }
    }

    fun dismissMergeGame() {
        mergeTimerJob?.cancel()
        val current = mergeGameState.value
        if (current != null && current.isActive) {
            // Save mid-game state
            viewModelScope.launch {
                gamificationRepository.saveMergeState(
                    current.grid, current.score,
                    current.timeRemainingSeconds, current.themeName
                )
            }
        }
        mergeGameState.value = null
    }

    // Snackbar overlay state
    val snackbarMessage = MutableStateFlow<String?>(null)

    fun addTask(
        title: String,
        priority: com.example.data.TaskPriority = com.example.data.TaskPriority.MEDIUM,
        category: com.example.data.TaskCategory = com.example.data.TaskCategory.PERSONAL,
        dueDateMs: Long? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.insert(
                Task(
                    title = title,
                    priority = priority.name,
                    category = category.name,
                    dueDateMs = dueDateMs
                )
            )
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            taskRepository.update(updated)
            if (updated.isCompleted) {
                // Reward for completing task
                com.example.util.AudioPlayer.playClickSound()
                gamificationRepository.addReward(xpReward = 20, dgReward = 5)
                gamificationRepository.recordActivityForToday()
                showSnackbar("Task Complete! +20 XP, +5 DG")
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteById(task.id)
        }
    }
    
    fun updateWorkingMemory(text: String) {
        viewModelScope.launch {
            gamificationRepository.saveWorkingMemoryAnchor(text)
        }
    }

    val showConfetti = MutableStateFlow(false)

    fun saveWizardStep(step: Int) {
        viewModelScope.launch {
            gamificationRepository.saveWizardStep(step)
        }
    }

    fun saveProfile(
        name: String,
        focusMinutes: Int,
        peakEnergy: String,
        enabledCategories: Set<String>,
        adhdPresentation: String = "",
        coOccurring: Set<String> = emptySet(),
        takesMedication: String = "",
        activityLevel: String = "",
        physicalLimitations: Set<String> = emptySet(),
        dietaryRestrictions: Set<String> = emptySet(),
        rewardPreferences: List<String> = emptyList(),
        selectedTheme: String = "Cosmic Slate"
    ) {
        viewModelScope.launch {
            gamificationRepository.saveProfile(
                name, focusMinutes, peakEnergy, enabledCategories,
                adhdPresentation, coOccurring, takesMedication,
                activityLevel, physicalLimitations, dietaryRestrictions,
                rewardPreferences, selectedTheme
            )
        }
    }
    
    fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarMessage.value = message
            delay(3000)
            if (snackbarMessage.value == message) {
                snackbarMessage.value = null
            }
        }
    }
    
    fun dismissSnackbar() {
        snackbarMessage.value = null
    }

    // Timer Logic
    private var timerJob: kotlinx.coroutines.Job? = null
    
    fun startTimer(durationMinutes: Int) {
        val totalSeconds = durationMinutes * 60
        focusTimerState.value = TimerState(isRunning = true, remainingSeconds = totalSeconds, initialSeconds = totalSeconds)
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (focusTimerState.value.remainingSeconds > 0) {
                delay(1000)
                focusTimerState.value = focusTimerState.value.copy(
                    remainingSeconds = focusTimerState.value.remainingSeconds - 1
                )
            }
            if(focusTimerState.value.remainingSeconds == 0) {
                 completeTimer()
            }
        }
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        focusTimerState.value = TimerState()
    }
    
    private fun completeTimer() {
        focusTimerState.value = focusTimerState.value.copy(isRunning = false)
        viewModelScope.launch {
            com.example.util.AudioPlayer.playTimerCompleteSound()
            val min = focusTimerState.value.initialSeconds / 60
            val dgReward = min / 5
            val xpReward = min * 5
            gamificationRepository.addReward(xpReward, dgReward)
            gamificationRepository.recordActivityForToday()
            showSnackbar("Focus Session Complete! +$xpReward XP, +$dgReward DG")
        }
    }

    val activeBreakActivity = MutableStateFlow<com.example.data.BreakActivity?>(null)

    fun startBreakActivity(activity: com.example.data.BreakActivity) {
        viewModelScope.launch {
            if (gamificationRepository.spendDopamineGold(activity.cost)) {
                activeBreakActivity.value = activity
                showSnackbar("${activity.emoji} ${activity.name} started!")
            } else {
                showSnackbar("Not enough Dopamine Gold!")
            }
        }
    }

    fun dismissBreakActivity() {
        activeBreakActivity.value = null
    }

    fun buyTitle(titleId: String, cost: Int) {
        viewModelScope.launch {
            val state = gamificationState.value
            if (state.unlockedTitleIds.contains(titleId)) {
                gamificationRepository.equipTitle(titleId)
            } else {
                if (gamificationRepository.spendDopamineGold(cost)) {
                    gamificationRepository.unlockTitle(titleId)
                    gamificationRepository.equipTitle(titleId)
                    showSnackbar("Title unlocked!")
                } else {
                    showSnackbar("Not enough Dopamine Gold!")
                }
            }
        }
    }

    fun buyTheme(themeName: String, cost: Int) {
        viewModelScope.launch {
            val state = gamificationState.value
            if (state.unlockedThemes.contains(themeName)) {
                 gamificationRepository.setTheme(themeName)
            } else {
                 if (gamificationRepository.spendDopamineGold(cost)) {
                     gamificationRepository.unlockTheme(themeName)
                     gamificationRepository.setTheme(themeName)
                     showSnackbar("$themeName Unlocked!")
                 } else {
                     showSnackbar("Not enough Dopamine Gold!")
                 }
            }
        }
    }
}

data class MergeGameUiState(
    val grid: List<Int>,             // 16 cells, -1=empty, 0-8=tier
    val score: Int = 0,
    val timeRemainingSeconds: Int = 180,
    val themeName: String = "Cosmic Slate",
    val selectedIndex: Int? = null,
    val lastMergedIndex: Int? = null,
    val lastMergeTier: Int? = null,
    val isActive: Boolean = true
) {
    companion object {
        fun newGame(themeName: String): MergeGameUiState {
            val grid = MutableList(16) { -1 }
            // Seed with 6 random low-tier tiles
            val positions = (0..15).shuffled().take(6)
            positions.forEach { grid[it] = (0..1).random() }
            return MergeGameUiState(grid = grid, themeName = themeName)
        }

        fun fromSave(save: com.example.data.MergeSaveState): MergeGameUiState =
            MergeGameUiState(
                grid = save.grid,
                score = save.score,
                timeRemainingSeconds = save.timeRemainingSeconds,
                themeName = save.themeName
            )
    }
}

data class TimerState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 0,
    val initialSeconds: Int = 0
) {
    val progress: Float
       get() = if (initialSeconds == 0) 0f else
           1f - (remainingSeconds.toFloat() / initialSeconds.toFloat())
           
    val displayString: String
       get() {
           val m = remainingSeconds / 60
           val s = remainingSeconds % 60
           return String.format("%02d:%02d", m, s)
       }
}
