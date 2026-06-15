package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gamification_state")

data class MergeSaveState(
    val grid: List<Int>,
    val score: Int,
    val timeRemainingSeconds: Int,
    val themeName: String
)

data class GamificationState(
    val isLoading: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val dopamineGold: Int = 0,
    val dailyFlowStreak: Int = 0,
    val isProfileSetup: Boolean = false,
    val currentTheme: String = "Cosmic Slate",
    val unlockedThemes: Set<String> = setOf("Cosmic Slate"),
    val workingMemoryAnchor: String = "",
    val lastActiveDate: String = "",
    val equippedTitleId: String = "apprentice",
    val unlockedTitleIds: Set<String> = setOf("apprentice"),
    val userName: String = "",
    val preferredFocusMinutes: Int = 25,
    val peakEnergyTime: String = "Morning",
    val enabledCategories: Set<String> = setOf("WORK","HOME","PERSONAL","ERRANDS","HEALTH"),
    val wizardStep: Int = 0,
    val adhdPresentation: String = "",
    val coOccurring: Set<String> = emptySet(),
    val takesMedication: String = "",
    val activityLevel: String = "",
    val physicalLimitations: Set<String> = emptySet(),
    val dietaryRestrictions: Set<String> = emptySet(),
    val rewardPreferences: List<String> = emptyList(),
    val mergeHighScore: Int = 0
)

data class BreakActivity(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val duration: String,
    val cost: Int,
    val instructions: List<String>
)

data class TitleReward(
    val id: String,
    val title: String,
    val subtitle: String,
    val cost: Int
)

val AvailableBreakActivities = listOf(
    BreakActivity(
        id = "walk",
        name = "Micro Walk",
        emoji = "🚶",
        description = "A quick movement reset. Gets blood to your brain.",
        duration = "5 min",
        cost = 20,
        instructions = listOf(
            "Stand up and shake out your hands",
            "Walk to another room or outside",
            "Notice 3 things you can see",
            "Take 5 slow breaths",
            "Return to your task refreshed"
        )
    ),
    BreakActivity(
        id = "breathe",
        name = "Box Breathing",
        emoji = "🌬",
        description = "Regulate your nervous system in 4 minutes.",
        duration = "4 min",
        cost = 15,
        instructions = listOf(
            "Sit comfortably and close your eyes",
            "Inhale slowly for 4 counts",
            "Hold for 4 counts",
            "Exhale for 4 counts",
            "Hold for 4 counts",
            "Repeat 4 times"
        )
    ),
    BreakActivity(
        id = "stretch",
        name = "Desk Stretch",
        emoji = "🧘",
        description = "Release tension from sitting. Body scan.",
        duration = "5 min",
        cost = 20,
        instructions = listOf(
            "Roll your shoulders back 5 times",
            "Tilt your head gently side to side",
            "Reach arms overhead and stretch",
            "Twist gently left and right",
            "Shake out your hands and wrists"
        )
    ),
    BreakActivity(
        id = "water",
        name = "Hydration Reset",
        emoji = "💧",
        description = "Dehydration tanks focus. Fix it now.",
        duration = "2 min",
        cost = 10,
        instructions = listOf(
            "Get up and fill a full glass of water",
            "Drink it slowly — don't rush",
            "Splash cold water on your face",
            "Return to your desk"
        )
    ),
    BreakActivity(
        id = "doodle",
        name = "Free Doodle",
        emoji = "✏️",
        description = "Unstructured drawing rests the planning brain.",
        duration = "5 min",
        cost = 25,
        instructions = listOf(
            "Grab paper and a pen",
            "Draw whatever comes to mind — no rules",
            "Don't judge what you create",
            "Fill the page, then put the pen down"
        )
    ),
    BreakActivity(
        id = "music",
        name = "Music Moment",
        emoji = "🎵",
        description = "One song. Eyes closed. Full attention.",
        duration = "3-4 min",
        cost = 15,
        instructions = listOf(
            "Pick a song you love",
            "Close your eyes or look somewhere neutral",
            "Really listen — notice each instrument",
            "Let the song finish completely"
        )
    ),
    BreakActivity(
        id = "snack",
        name = "Brain Fuel",
        emoji = "🍎",
        description = "Low blood sugar kills focus. Eat something real.",
        duration = "5 min",
        cost = 20,
        instructions = listOf(
            "Get away from your desk",
            "Eat a small snack — fruit, nuts, or protein",
            "No screens while eating",
            "Notice the taste and texture"
        )
    )
)

val AvailableTitles = listOf(
    TitleReward("apprentice", "Apprentice", "Just getting started", 0),
    TitleReward("focus_seeker", "Focus Seeker", "Learning the craft", 50),
    TitleReward("task_tamer", "Task Tamer", "Tasks don't scare you anymore", 150),
    TitleReward("flow_rider", "Flow Rider", "You've found your rhythm", 300),
    TitleReward("chaos_calmer", "Chaos Calmer", "Order from disorder", 500),
    TitleReward("hyperfocus_hero", "Hyperfocus Hero", "Power and precision", 800),
    TitleReward("executive_legend", "Executive Legend", "Peak performance", 1200)
)

data class MergeSymbolSet(
    val themeName: String,
    val symbols: List<String>  // index = tier (0 = lowest, 8 = highest)
)

val MergeSymbolSets = listOf(
    MergeSymbolSet("Cosmic Slate",
        listOf("⭐","🌟","💫","☄️","🌙","🪐","🌌","🔮","👑")),
    MergeSymbolSet("Forest Sanctuary",
        listOf("🌱","🌿","🍃","🌸","🌺","🍄","🌳","🦋","🌈")),
    MergeSymbolSet("Cyber Oasis",
        listOf("⚡","🔋","💡","📡","🖥️","🤖","🛸","💎","🌐"))
)

fun getSymbolSet(themeName: String): MergeSymbolSet =
    MergeSymbolSets.find { it.themeName == themeName }
        ?: MergeSymbolSets.first()

class GamificationRepository(private val dataStore: DataStore<Preferences>) {
    private val XP_KEY = intPreferencesKey("xp")
    private val LEVEL_KEY = intPreferencesKey("level")
    private val DG_KEY = intPreferencesKey("dopamine_gold")
    private val STREAK_KEY = intPreferencesKey("daily_streak")
    private val PROFILE_SETUP_KEY = booleanPreferencesKey("profile_setup")
    private val THEME_KEY = stringPreferencesKey("current_theme")
    // Use comma separated for simple set emulation
    private val UNLOCKED_THEMES_KEY = stringPreferencesKey("unlocked_themes")
    private val ANCHOR_KEY = stringPreferencesKey("working_memory_anchor")
    private val LAST_DATE_KEY = stringPreferencesKey("last_active_date")
    private val TITLE_KEY = stringPreferencesKey("equipped_title")
    private val UNLOCKED_TITLES_KEY = stringPreferencesKey("unlocked_titles")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val FOCUS_MINS_KEY = intPreferencesKey("preferred_focus_minutes")
    private val PEAK_ENERGY_KEY = stringPreferencesKey("peak_energy_time")
    private val ENABLED_CATS_KEY = stringPreferencesKey("enabled_categories")
    private val WIZARD_STEP_KEY = intPreferencesKey("wizard_step")
    private val ADHD_KEY = stringPreferencesKey("adhd_presentation")
    private val CO_KEY = stringPreferencesKey("co_occurring")
    private val MED_KEY = stringPreferencesKey("medication")
    private val ACTIVITY_KEY = stringPreferencesKey("activity_level")
    private val PHYSICAL_KEY = stringPreferencesKey("physical_limits")
    private val DIET_KEY = stringPreferencesKey("dietary")
    private val REWARDS_KEY = stringPreferencesKey("reward_prefs")
    private val MERGE_GRID_KEY = stringPreferencesKey("merge_grid")
    private val MERGE_SCORE_KEY = intPreferencesKey("merge_score")
    private val MERGE_HIGH_SCORE_KEY = intPreferencesKey("merge_high_score")
    private val MERGE_THEME_KEY = stringPreferencesKey("merge_session_theme")
    private val MERGE_TIME_KEY = intPreferencesKey("merge_time_remaining")
    
    val stateFlow: Flow<GamificationState> = dataStore.data.map { prefs ->
        GamificationState(
            isLoading = false,
            xp = prefs[XP_KEY] ?: 0,
            level = prefs[LEVEL_KEY] ?: 1,
            dopamineGold = prefs[DG_KEY] ?: 0,
            dailyFlowStreak = prefs[STREAK_KEY] ?: 0,
            isProfileSetup = prefs[PROFILE_SETUP_KEY] ?: false,
            currentTheme = prefs[THEME_KEY] ?: "Cosmic Slate",
            unlockedThemes = prefs[UNLOCKED_THEMES_KEY]?.split(",")?.toSet() ?: setOf("Cosmic Slate"),
            workingMemoryAnchor = prefs[ANCHOR_KEY] ?: "",
            lastActiveDate = prefs[LAST_DATE_KEY] ?: "",
            equippedTitleId = prefs[TITLE_KEY] ?: "apprentice",
            unlockedTitleIds = prefs[UNLOCKED_TITLES_KEY]?.split(",")?.toSet() ?: setOf("apprentice"),
            userName = prefs[USER_NAME_KEY] ?: "",
            preferredFocusMinutes = prefs[FOCUS_MINS_KEY] ?: 25,
            peakEnergyTime = prefs[PEAK_ENERGY_KEY] ?: "Morning",
            enabledCategories = prefs[ENABLED_CATS_KEY]?.split(",")?.toSet()
                ?: setOf("WORK","HOME","PERSONAL","ERRANDS","HEALTH"),
            wizardStep = prefs[WIZARD_STEP_KEY] ?: 0,
            adhdPresentation = prefs[ADHD_KEY] ?: "",
            coOccurring = prefs[CO_KEY]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet(),
            takesMedication = prefs[MED_KEY] ?: "",
            activityLevel = prefs[ACTIVITY_KEY] ?: "",
            physicalLimitations = prefs[PHYSICAL_KEY]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet(),
            dietaryRestrictions = prefs[DIET_KEY]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet(),
            rewardPreferences = prefs[REWARDS_KEY]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
            mergeHighScore = prefs[MERGE_HIGH_SCORE_KEY] ?: 0
        )
    }
    
    suspend fun addReward(xpReward: Int, dgReward: Int) {
        dataStore.edit { prefs ->
            val currentXp = prefs[XP_KEY] ?: 0
            val currentLevel = prefs[LEVEL_KEY] ?: 1
            val currentDg = prefs[DG_KEY] ?: 0
            
            val nextXp = currentXp + xpReward
            val xpNeeded = currentLevel * 100
            
            if (nextXp >= xpNeeded) {
                prefs[LEVEL_KEY] = currentLevel + 1
                prefs[XP_KEY] = nextXp - xpNeeded
                com.example.util.AudioPlayer.playLevelUpSound()
            } else {
                prefs[XP_KEY] = nextXp
            }
            
            prefs[DG_KEY] = currentDg + dgReward
        }
    }
    
    suspend fun spendDopamineGold(amount: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val currentDg = prefs[DG_KEY] ?: 0
            if (currentDg >= amount) {
                prefs[DG_KEY] = currentDg - amount
                success = true
            }
        }
        return success
    }
    
    suspend fun unlockTheme(themeName: String) {
        dataStore.edit { prefs ->
            val currentUnlocked = prefs[UNLOCKED_THEMES_KEY]?.split(",")?.toSet() ?: setOf("Cosmic Slate")
            val nextUnlocked = currentUnlocked + themeName
            prefs[UNLOCKED_THEMES_KEY] = nextUnlocked.joinToString(",")
        }
    }
    
    suspend fun setTheme(themeName: String) {
       dataStore.edit { prefs ->
           prefs[THEME_KEY] = themeName
       }
    }

    suspend fun saveProfile(
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
        dataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name.trim()
            prefs[FOCUS_MINS_KEY] = focusMinutes
            prefs[PEAK_ENERGY_KEY] = peakEnergy
            prefs[ENABLED_CATS_KEY] = enabledCategories.joinToString(",")
            prefs[ADHD_KEY] = adhdPresentation
            prefs[CO_KEY] = coOccurring.joinToString(",")
            prefs[MED_KEY] = takesMedication
            prefs[ACTIVITY_KEY] = activityLevel
            prefs[PHYSICAL_KEY] = physicalLimitations.joinToString(",")
            prefs[DIET_KEY] = dietaryRestrictions.joinToString(",")
            prefs[REWARDS_KEY] = rewardPreferences.joinToString(",")
            prefs[THEME_KEY] = selectedTheme
            val currentThemes = prefs[UNLOCKED_THEMES_KEY]?.split(",")?.toSet() ?: setOf("Cosmic Slate")
            prefs[UNLOCKED_THEMES_KEY] = (currentThemes + selectedTheme).joinToString(",")
            prefs[PROFILE_SETUP_KEY] = true
        }
    }

    suspend fun saveWorkingMemoryAnchor(text: String) {
        dataStore.edit { prefs ->
            prefs[ANCHOR_KEY] = text
        }
    }

    suspend fun saveWizardStep(step: Int) {
        dataStore.edit { prefs ->
            prefs[WIZARD_STEP_KEY] = step
        }
    }

    suspend fun clearWizardStep() {
        dataStore.edit { prefs ->
            prefs[WIZARD_STEP_KEY] = 0
        }
    }

    fun getTodayDateString(): String {
        val cal = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    fun getYesterdayDateString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    suspend fun recordActivityForToday() {
        dataStore.edit { prefs ->
            val today = getTodayDateString()
            val lastDate = prefs[LAST_DATE_KEY] ?: ""
            val currentStreak = prefs[STREAK_KEY] ?: 0

            when {
                lastDate == today -> {
                    // Already recorded today — no change
                }
                lastDate == getYesterdayDateString() -> {
                    // Consecutive day — increment streak
                    prefs[STREAK_KEY] = currentStreak + 1
                    prefs[LAST_DATE_KEY] = today
                }
                else -> {
                    // First ever use or missed a day — start/restart streak at 1
                    prefs[STREAK_KEY] = 1
                    prefs[LAST_DATE_KEY] = today
                }
            }
        }
    }

    suspend fun unlockTitle(titleId: String) {
        dataStore.edit { prefs ->
            val current = prefs[UNLOCKED_TITLES_KEY]?.split(",")?.toSet() ?: setOf("apprentice")
            prefs[UNLOCKED_TITLES_KEY] = (current + titleId).joinToString(",")
        }
    }

    suspend fun equipTitle(titleId: String) {
        dataStore.edit { prefs ->
            prefs[TITLE_KEY] = titleId
        }
    }

    suspend fun saveMergeState(
        grid: List<Int>,   // -1 = empty, 0-8 = tier
        score: Int,
        timeRemainingSeconds: Int,
        themeName: String
    ) {
        dataStore.edit { prefs ->
            prefs[MERGE_GRID_KEY] = grid.joinToString(",")
            prefs[MERGE_SCORE_KEY] = score
            prefs[MERGE_TIME_KEY] = timeRemainingSeconds
            prefs[MERGE_THEME_KEY] = themeName
        }
    }

    suspend fun loadMergeState(): MergeSaveState? {
        val prefs = dataStore.data.first()
        val gridStr = prefs[MERGE_GRID_KEY] ?: return null
        val grid = gridStr.split(",").mapNotNull { it.toIntOrNull() }
        if (grid.size != 16) return null
        return MergeSaveState(
            grid = grid,
            score = prefs[MERGE_SCORE_KEY] ?: 0,
            timeRemainingSeconds = prefs[MERGE_TIME_KEY] ?: 180,
            themeName = prefs[MERGE_THEME_KEY] ?: "Cosmic Slate"
        )
    }

    suspend fun clearMergeState() {
        dataStore.edit { prefs ->
            prefs.remove(MERGE_GRID_KEY)
            prefs.remove(MERGE_SCORE_KEY)
            prefs.remove(MERGE_TIME_KEY)
            prefs.remove(MERGE_THEME_KEY)
        }
    }

    suspend fun updateMergeHighScore(score: Int) {
        dataStore.edit { prefs ->
            val current = prefs[MERGE_HIGH_SCORE_KEY] ?: 0
            if (score > current) prefs[MERGE_HIGH_SCORE_KEY] = score
        }
    }
}
