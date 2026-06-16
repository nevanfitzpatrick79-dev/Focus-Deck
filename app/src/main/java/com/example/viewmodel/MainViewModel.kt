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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class PuzzlePhase {
    SHOWING,    // Pattern is visible — memorise it
    RECALLING,  // Pattern hidden — tap to recreate
    CORRECT,    // Brief success flash before next round
    WRONG,      // Brief error flash, retry same round
    COMPLETE    // Game over
}

data class PuzzleGameUiState(
    val grid: List<Boolean> = List(9) { false }, // 3x3, true=lit
    val playerGrid: List<Boolean> = List(9) { false }, // player's taps
    val phase: PuzzlePhase = PuzzlePhase.SHOWING,
    val level: Int = 1,           // cells to remember = level + 1
    val score: Int = 0,
    val timeRemainingSeconds: Int = 180,
    val themeName: String = "Cosmic Slate",
    val isActive: Boolean = true,
    val showTimer: Int = 2        // seconds to show pattern
) {
    val cellsToShow: Int get() = (level + 1).coerceAtMost(9)

    companion object {
        fun newGame(themeName: String): PuzzleGameUiState {
            val state = PuzzleGameUiState(themeName = themeName)
            return state.withNewPattern()
        }
    }

    fun withNewPattern(): PuzzleGameUiState {
        val indices = (0..8).shuffled().take(cellsToShow)
        val newGrid = List(9) { i -> i in indices }
        return copy(
            grid = newGrid,
            playerGrid = List(9) { false },
            phase = PuzzlePhase.SHOWING,
            showTimer = 2
        )
    }

    fun isPatternComplete(): Boolean {
        return playerGrid.zip(grid).all { (p, g) -> p == g }
    }

    fun wrongCells(): List<Int> {
        return playerGrid.indices.filter { i ->
            playerGrid[i] != grid[i]
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "focus-deck-db"
    ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
     .build()

    private val taskRepository = TaskRepository(db.taskDao())
    private val gamificationRepository = GamificationRepository(application.dataStore)

    val newlyEarnedBadge = MutableStateFlow<String?>(null)

    fun dismissBadge() { newlyEarnedBadge.value = null }

    val selectedCategory = MutableStateFlow(com.example.data.TaskCategory.ALL)

    fun saveNotificationSettings(
        focusCheckInEnabled: Boolean,
        focusCheckInIntervalMinutes: Int,
        focusCheckInStartHour: Int,
        focusCheckInEndHour: Int,
        taskNudgeEnabled: Boolean,
        taskNudgeHour: Int,
        streakProtectorEnabled: Boolean,
        streakProtectorHour: Int,
        medicationReminderEnabled: Boolean,
        medicationReminderHour: Int,
        medicationReminderMinute: Int,
        quietHoursEnabled: Boolean,
        quietHoursStartHour: Int,
        quietHoursEndHour: Int,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            gamificationRepository.saveNotificationSettings(
                focusCheckInEnabled, focusCheckInIntervalMinutes,
                focusCheckInStartHour, focusCheckInEndHour,
                taskNudgeEnabled, taskNudgeHour,
                streakProtectorEnabled, streakProtectorHour,
                medicationReminderEnabled, medicationReminderHour, medicationReminderMinute,
                quietHoursEnabled, quietHoursStartHour, quietHoursEndHour
            )
            // Reschedule WorkManager after saving
            val newState = gamificationRepository.stateFlow.first()
            com.example.notifications.ReminderScheduler.rescheduleAll(context, newState)
        }
    }

    fun resetProfile(context: android.content.Context) {
        viewModelScope.launch {
            gamificationRepository.resetProfile()
            com.example.notifications.ReminderScheduler.cancelAll(context)
        }
    }

    fun completeTutorial() {
        viewModelScope.launch {
            gamificationRepository.completeTutorial()
        }
    }

    fun addTutorialTask(title: String): Boolean {
        if (title.isBlank()) return false
        viewModelScope.launch {
            taskRepository.insert(
                Task(
                    title = title,
                    priority = com.example.data.TaskPriority.HIGH.name,
                    category = com.example.data.TaskCategory.PERSONAL.name
                )
            )
        }
        return true
    }

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

    val puzzleGameState = MutableStateFlow<PuzzleGameUiState?>(null)
    private var puzzleTimerJob: kotlinx.coroutines.Job? = null
    private var puzzleShowJob: kotlinx.coroutines.Job? = null

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
            
            gamificationRepository.recordGamePlayed()
            val badge = gamificationRepository.checkAndAwardBadges()
            if (badge != null) newlyEarnedBadge.value = badge
            
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

    fun startPuzzleGame(themeName: String) {
        puzzleGameState.value = PuzzleGameUiState.newGame(themeName)
        startPuzzleTimer()
        startShowCountdown()
    }

    private fun startPuzzleTimer() {
        puzzleTimerJob?.cancel()
        puzzleTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = puzzleGameState.value ?: break
                if (!current.isActive) break
                val newTime = current.timeRemainingSeconds - 1
                if (newTime <= 0) {
                    endPuzzleGame()
                    break
                } else {
                    puzzleGameState.value = current.copy(timeRemainingSeconds = newTime)
                }
            }
        }
    }

    private fun startShowCountdown() {
        puzzleShowJob?.cancel()
        puzzleShowJob = viewModelScope.launch {
            delay(2000) // Show pattern for 2 seconds
            val current = puzzleGameState.value ?: return@launch
            if (current.phase == PuzzlePhase.SHOWING) {
                puzzleGameState.value = current.copy(phase = PuzzlePhase.RECALLING)
            }
        }
    }

    fun onPuzzleTap(index: Int) {
        val current = puzzleGameState.value ?: return
        if (current.phase != PuzzlePhase.RECALLING) return

        // Toggle this cell
        val newPlayerGrid = current.playerGrid.toMutableList()
        newPlayerGrid[index] = !newPlayerGrid[index]
        val updated = current.copy(playerGrid = newPlayerGrid)

        // Check if player has tapped exactly cellsToShow cells
        val tappedCount = newPlayerGrid.count { it }
        if (tappedCount == current.cellsToShow) {
            // Auto-evaluate when enough cells are tapped
            if (updated.isPatternComplete()) {
                // Correct!
                val newScore = current.score + current.level
                puzzleGameState.value = updated.copy(
                    phase = PuzzlePhase.CORRECT,
                    score = newScore
                )
                viewModelScope.launch {
                    delay(800)
                    val next = puzzleGameState.value ?: return@launch
                    puzzleGameState.value = next
                        .copy(level = next.level + 1)
                        .withNewPattern()
                    startShowCountdown()
                }
            } else {
                // Wrong
                puzzleGameState.value = updated.copy(phase = PuzzlePhase.WRONG)
                viewModelScope.launch {
                    delay(1000)
                    val retry = puzzleGameState.value ?: return@launch
                    puzzleGameState.value = retry.withNewPattern()
                    startShowCountdown()
                }
            }
        } else {
            puzzleGameState.value = updated
        }
    }

    fun endPuzzleGame() {
        puzzleTimerJob?.cancel()
        puzzleShowJob?.cancel()
        val current = puzzleGameState.value ?: return
        val finalScore = current.score
        puzzleGameState.value = current.copy(
            isActive = false,
            phase = PuzzlePhase.COMPLETE
        )
        viewModelScope.launch {
            gamificationRepository.recordGamePlayed()
            val dgReward = (finalScore / 5).coerceAtMost(30)
            val xpReward = (finalScore * 3).coerceAtMost(60)
            if (dgReward > 0 || xpReward > 0) {
                gamificationRepository.addReward(xpReward, dgReward)
                showSnackbar("🧩 Score: $finalScore — +$xpReward XP, +$dgReward DG")
            }
            val badge = gamificationRepository.checkAndAwardBadges()
            if (badge != null) newlyEarnedBadge.value = badge
        }
    }

    fun dismissPuzzleGame() {
        puzzleTimerJob?.cancel()
        puzzleShowJob?.cancel()
        puzzleGameState.value = null
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
                gamificationRepository.recordTaskCompleted()
                val badge = gamificationRepository.checkAndAwardBadges()
                if (badge != null) newlyEarnedBadge.value = badge
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
        focusTimerState.value = TimerState(isRunning = true, remainingSeconds = totalSeconds, initialSeconds = totalSeconds, totalDurationMinutes = durationMinutes)
        
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
            gamificationRepository.recordFocusSession(focusTimerState.value.totalDurationMinutes)
            val badge = gamificationRepository.checkAndAwardBadges()
            if (badge != null) newlyEarnedBadge.value = badge
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

    // ——————————————————————————————————————————————————————————
    // Easter Eggs & Mystery Box
    // ——————————————————————————————————————————————————————————

    // Easter egg: long-press on streak fires confetti and grants DG
    fun onStreakLongPress() {
        viewModelScope.launch {
            val isNew = gamificationRepository.discoverEgg("streak_longpress", dgReward = 5)
            if (isNew) {
                showSnackbar("✨ Secret found! +5 Dopamine Gold")
                // Trigger confetti
                showConfetti.value = true
                kotlinx.coroutines.delay(3000)
                showConfetti.value = false
            } else {
                showSnackbar("🔥 Keep that streak going!")
            }
        }
    }

    private var titleTapCount = 0

    // Easter egg: tap title text 7 times to unlock Hacker title
    fun onTitleTapped() {
        titleTapCount++
        if (titleTapCount >= 7) {
            titleTapCount = 0
            viewModelScope.launch {
                val isNew = gamificationRepository.discoverEgg("hacker_title", dgReward = 0)
                if (isNew) {
                    gamificationRepository.unlockHiddenTitle("hacker")
                    showSnackbar("💻 Secret unlocked: Hacker title available in Shop!")
                }
            }
        }
    }

    // Easter egg: midnight bonus
    fun checkMidnightEgg() {
        val hour = java.util.Calendar.getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)
        val minute = java.util.Calendar.getInstance()
            .get(java.util.Calendar.MINUTE)
        if (hour == 0 && minute < 5) {
            viewModelScope.launch {
                val isNew = gamificationRepository.discoverEgg("midnight", dgReward = 10)
                if (isNew) {
                    gamificationRepository.unlockHiddenTitle("night_owl")
                    showSnackbar("🌙 Night Owl! +10 DG and a secret title unlocked.")
                }
            }
        }
    }

    // Easter egg: Halloween
    fun checkHalloweenEgg() {
        val cal = java.util.Calendar.getInstance()
        val isHalloween = cal.get(java.util.Calendar.MONTH) == 9 &&
            cal.get(java.util.Calendar.DAY_OF_MONTH) == 31
        if (isHalloween) {
            viewModelScope.launch {
                gamificationRepository.discoverEgg("halloween", dgReward = 31)
                showSnackbar("🎃 Happy Halloween! +31 DG")
            }
        }
    }

    // Easter egg: hyperfocus anchor text
    fun checkAnchorEasterEgg(text: String) {
        if (text.lowercase().contains("hyperfocus")) {
            viewModelScope.launch {
                val isNew = gamificationRepository.discoverEgg("hyperfocus_anchor", dgReward = 10)
                if (isNew) {
                    gamificationRepository.unlockHiddenTitle("hyperfocused")
                    showSnackbar("🧠 Oh, we know this one. +10 DG and a secret title.")
                }
            }
        }
    }

    // Mystery box
    fun claimMysteryBox() {
        viewModelScope.launch {
            val result = gamificationRepository.claimMysteryBox()
            when {
                result == "already_claimed" ->
                    showSnackbar("Mystery Box resets tomorrow!")
                result.startsWith("dg_") -> {
                    val amount = result.removePrefix("dg_").toIntOrNull() ?: 0
                    showSnackbar("📦 Mystery Box: +$amount Dopamine Gold!")
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
    val initialSeconds: Int = 0,
    val totalDurationMinutes: Int = 25
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
