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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "focus-deck-db"
    ).build()

    private val taskRepository = TaskRepository(db.taskDao())
    private val gamificationRepository = GamificationRepository(application.dataStore)

    val tasks: StateFlow<List<Task>> = taskRepository.allTasks.stateIn(
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
    
    val workingMemoryAnchor = MutableStateFlow("")

    val focusTimerState = MutableStateFlow(TimerState())
    
    // Snackbar overlay state
    val snackbarMessage = MutableStateFlow<String?>(null)

    fun addTask(title: String) {
        if(title.isBlank()) return
        viewModelScope.launch {
            taskRepository.insert(Task(title = title))
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
        workingMemoryAnchor.value = text
    }

    val showConfetti = MutableStateFlow(false)

    fun completeProfileSetup() {
        viewModelScope.launch {
             gamificationRepository.setProfileSetupComplete()
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
            showSnackbar("Focus Session Complete! +$xpReward XP, +$dgReward DG")
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
