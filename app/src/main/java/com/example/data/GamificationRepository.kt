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
    val tutorialCompleted: Boolean = false,
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
    val mergeHighScore: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalFocusSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalGamesPlayed: Int = 0,
    val earnedBadgeIds: Set<String> = emptySet(),
    val focusCheckInEnabled: Boolean = false,
    val focusCheckInIntervalMinutes: Int = 60,
    val focusCheckInStartHour: Int = 9,
    val focusCheckInEndHour: Int = 18,
    val taskNudgeEnabled: Boolean = false,
    val taskNudgeHour: Int = 14,
    val streakProtectorEnabled: Boolean = false,
    val streakProtectorHour: Int = 16,
    val medicationReminderEnabled: Boolean = false,
    val medicationReminderHour: Int = 8,
    val medicationReminderMinute: Int = 0,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22,
    val quietHoursEndHour: Int = 8
)

data class BreakActivity(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val duration: String,
    val cost: Int,
    val instructions: List<String>,
    val requiresMovement: Boolean = false,   // blocked by Limited mobility / Chronic pain
    val requiresVision: Boolean = false,     // blocked by Vision impairment
    val requiresHearing: Boolean = false,    // blocked by Hearing impairment
    val containsFood: Boolean = false,       // gated by dietary restrictions
    val foodRestrictions: Set<String> = emptySet(), // which dietary tags block this
    val category: String = "rest"            // "movement", "breathing", "creative", "rest", "food"
)

data class TitleReward(
    val id: String,
    val title: String,
    val subtitle: String,
    val cost: Int
)

val AvailableBreakActivities = listOf(
    BreakActivity(
        id = "walk", name = "Micro Walk", emoji = "🚶",
        description = "A quick movement reset. Gets blood to your brain.",
        duration = "5 min", cost = 20,
        instructions = listOf(
            "Stand up and shake out your hands",
            "Walk to another room or outside",
            "Notice 3 things you can see",
            "Take 5 slow breaths",
            "Return to your task refreshed"
        ),
        requiresMovement = true,
        category = "movement"
    ),
    BreakActivity(
        id = "breathe", name = "Box Breathing", emoji = "🌬",
        description = "Regulate your nervous system in 4 minutes.",
        duration = "4 min", cost = 15,
        instructions = listOf(
            "Sit comfortably and close your eyes",
            "Inhale slowly for 4 counts",
            "Hold for 4 counts",
            "Exhale for 4 counts",
            "Hold for 4 counts",
            "Repeat 4 times"
        ),
        category = "breathing"
    ),
    BreakActivity(
        id = "stretch", name = "Desk Stretch", emoji = "🧘",
        description = "Release tension from sitting. Body scan.",
        duration = "5 min", cost = 20,
        instructions = listOf(
            "Roll your shoulders back 5 times",
            "Tilt your head gently side to side",
            "Reach arms overhead and stretch",
            "Twist gently left and right",
            "Shake out your hands and wrists"
        ),
        requiresMovement = true,
        category = "movement"
    ),
    BreakActivity(
        id = "water", name = "Hydration Reset", emoji = "💧",
        description = "Dehydration tanks focus. Fix it now.",
        duration = "2 min", cost = 10,
        instructions = listOf(
            "Get up and fill a full glass of water",
            "Drink it slowly — don't rush",
            "Splash cold water on your face",
            "Return to your desk"
        ),
        requiresMovement = true,
        category = "rest"
    ),
    BreakActivity(
        id = "doodle", name = "Free Doodle", emoji = "✏️",
        description = "Unstructured drawing rests the planning brain.",
        duration = "5 min", cost = 25,
        instructions = listOf(
            "Grab paper and a pen",
            "Draw whatever comes to mind — no rules",
            "Don't judge what you create",
            "Fill the page, then put the pen down"
        ),
        requiresVision = true,
        category = "creative"
    ),
    BreakActivity(
        id = "music", name = "Music Moment", emoji = "🎵",
        description = "One song. Eyes closed. Full attention.",
        duration = "3-4 min", cost = 15,
        instructions = listOf(
            "Pick a song you love",
            "Close your eyes or look somewhere neutral",
            "Really listen — notice each instrument",
            "Let the song finish completely"
        ),
        requiresHearing = true,
        category = "rest"
    ),
    BreakActivity(
        id = "snack", name = "Brain Fuel", emoji = "🍎",
        description = "Low blood sugar kills focus. Eat something real.",
        duration = "5 min", cost = 20,
        instructions = listOf(
            "Get away from your desk",
            "Eat a small snack — fruit, nuts, or protein",
            "No screens while eating",
            "Notice the taste and texture"
        ),
        containsFood = true,
        foodRestrictions = setOf("Diabetic"),  // snack suggestion may need to change
        category = "food"
    ),
    BreakActivity(
        id = "meditate", name = "Mini Meditation", emoji = "🧘",
        description = "Two minutes. Sit still. Notice your breath.",
        duration = "2 min", cost = 10,
        instructions = listOf(
            "Sit comfortably with both feet on the floor",
            "Close your eyes or soften your gaze",
            "Breathe naturally — don't control it",
            "When your mind wanders, gently return to your breath",
            "After 2 minutes, slowly open your eyes"
        ),
        category = "breathing"
    ),
    BreakActivity(
        id = "gratitude", name = "Three Good Things", emoji = "✨",
        description = "A 90-second mood reset backed by research.",
        duration = "2 min", cost = 10,
        instructions = listOf(
            "Grab a piece of paper or open a notes app",
            "Write three specific things that went okay today",
            "They don't have to be big — small counts",
            "Read them back once",
            "Return to work"
        ),
        category = "rest"
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

data class Badge(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val dgReward: Int,
    val isEarned: (tasks: Int, sessions: Int, focusMins: Int,
                   streak: Int, games: Int, level: Int) -> Boolean
)

val AvailableBadges = listOf(
    // Task badges
    Badge("first_task", "First Step", "✅",
        "Complete your very first task.", 10,
        { tasks, _, _, _, _, _ -> tasks >= 1 }),
    Badge("tasks_10", "Getting Things Done", "📋",
        "Complete 10 tasks.", 15,
        { tasks, _, _, _, _, _ -> tasks >= 10 }),
    Badge("tasks_50", "Task Tamer", "⚡",
        "Complete 50 tasks.", 25,
        { tasks, _, _, _, _, _ -> tasks >= 50 }),
    Badge("tasks_100", "Centurion", "💯",
        "Complete 100 tasks.", 50,
        { tasks, _, _, _, _, _ -> tasks >= 100 }),
    Badge("tasks_500", "Unstoppable", "🏆",
        "Complete 500 tasks.", 100,
        { tasks, _, _, _, _, _ -> tasks >= 500 }),

    // Focus session badges
    Badge("first_session", "In the Zone", "⏱",
        "Complete your first focus session.", 10,
        { _, sessions, _, _, _, _ -> sessions >= 1 }),
    Badge("sessions_10", "Focus Habit", "🎯",
        "Complete 10 focus sessions.", 20,
        { _, sessions, _, _, _, _ -> sessions >= 10 }),
    Badge("focus_60mins", "Deep Work", "🧠",
        "Accumulate 60 minutes of focused work.", 25,
        { _, _, mins, _, _, _ -> mins >= 60 }),
    Badge("focus_300mins", "Flow State", "🌊",
        "Accumulate 5 hours of focused work.", 50,
        { _, _, mins, _, _, _ -> mins >= 300 }),
    Badge("focus_1000mins", "Marathon Mind", "🏅",
        "Accumulate over 16 hours of focused work.", 100,
        { _, _, mins, _, _, _ -> mins >= 1000 }),

    // Streak badges
    Badge("streak_3", "Consistent", "🔥",
        "Maintain a 3-day streak.", 15,
        { _, _, _, streak, _, _ -> streak >= 3 }),
    Badge("streak_7", "Week Warrior", "🗓",
        "Maintain a 7-day streak.", 30,
        { _, _, _, streak, _, _ -> streak >= 7 }),
    Badge("streak_30", "Month Strong", "📅",
        "Maintain a 30-day streak.", 75,
        { _, _, _, streak, _, _ -> streak >= 30 }),

    // Level badges
    Badge("level_5", "Rising", "⭐",
        "Reach Level 5.", 20,
        { _, _, _, _, _, level -> level >= 5 }),
    Badge("level_10", "Experienced", "🌟",
        "Reach Level 10.", 40,
        { _, _, _, _, _, level -> level >= 10 }),
    Badge("level_25", "Veteran", "💫",
        "Reach Level 25.", 75,
        { _, _, _, _, _, level -> level >= 25 }),

    // Game badges
    Badge("first_game", "Player", "🎮",
        "Play your first merge game.", 10,
        { _, _, _, _, games, _ -> games >= 1 }),
    Badge("games_10", "Gamer", "🕹",
        "Play 10 merge games.", 20,
        { _, _, _, _, games, _ -> games >= 10 })
)

fun getFilteredBreakActivities(state: GamificationState): List<BreakActivity> {
    val limitations = state.physicalLimitations
    val diet = state.dietaryRestrictions
    val prefs = state.rewardPreferences

    // Filter out activities the user can't do
    val filtered = AvailableBreakActivities.filter { activity ->
        val blockedByMobility = activity.requiresMovement &&
            (limitations.contains("Limited mobility") || limitations.contains("Chronic pain"))
        val blockedByVision = activity.requiresVision &&
            limitations.contains("Vision impairment")
        val blockedByHearing = activity.requiresHearing &&
            limitations.contains("Hearing impairment")
        val blockedByDiet = activity.containsFood &&
            activity.foodRestrictions.any { diet.contains(it) }

        !blockedByMobility && !blockedByVision && !blockedByHearing && !blockedByDiet
    }

    // Sort by reward preferences
    // Map preference labels to activity categories
    val categoryOrder = prefs.mapIndexedNotNull { index, pref ->
        val cat = when (pref) {
            "Movement" -> "movement"
            "Rest" -> "rest"
            "Creative" -> "creative"
            "Music" -> "rest"  // music falls under rest category
            else -> null
        }
        cat?.let { it to index }
    }.toMap()

    return filtered.sortedBy { activity ->
        categoryOrder[activity.category] ?: Int.MAX_VALUE
    }
}

fun getPersonalisedSnackInstructions(state: GamificationState): List<String> {
    val diet = state.dietaryRestrictions
    return when {
        diet.contains("Diabetic") -> listOf(
            "Get away from your desk",
            "Choose a low-sugar snack — nuts, cheese, or vegetables",
            "Avoid fruit juice or anything sweetened",
            "No screens while eating",
            "Notice the taste and texture"
        )
        diet.contains("Vegan") -> listOf(
            "Get away from your desk",
            "Eat a plant-based snack — fruit, nuts, or hummus with veg",
            "No screens while eating",
            "Notice the taste and texture"
        )
        diet.contains("Nut allergy") -> listOf(
            "Get away from your desk",
            "Choose a nut-free snack — fruit, cheese, or seeds",
            "Check labels if it's packaged",
            "No screens while eating",
            "Notice the taste and texture"
        )
        else -> listOf(
            "Get away from your desk",
            "Eat a small snack — fruit, nuts, or protein",
            "No screens while eating",
            "Notice the taste and texture"
        )
    }
}

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

    private val FOCUS_CHECKIN_ENABLED_KEY = booleanPreferencesKey("focus_checkin_enabled")
    private val FOCUS_CHECKIN_INTERVAL_KEY = intPreferencesKey("focus_checkin_interval")
    private val FOCUS_CHECKIN_START_KEY = intPreferencesKey("focus_checkin_start")
    private val FOCUS_CHECKIN_END_KEY = intPreferencesKey("focus_checkin_end")
    private val TASK_NUDGE_ENABLED_KEY = booleanPreferencesKey("task_nudge_enabled")
    private val TASK_NUDGE_HOUR_KEY = intPreferencesKey("task_nudge_hour")
    private val STREAK_PROTECTOR_ENABLED_KEY = booleanPreferencesKey("streak_protector_enabled")
    private val STREAK_PROTECTOR_HOUR_KEY = intPreferencesKey("streak_protector_hour")
    private val MED_REMINDER_ENABLED_KEY = booleanPreferencesKey("med_reminder_enabled")
    private val MED_REMINDER_HOUR_KEY = intPreferencesKey("med_reminder_hour")
    private val MED_REMINDER_MINUTE_KEY = intPreferencesKey("med_reminder_minute")
    private val QUIET_HOURS_ENABLED_KEY = booleanPreferencesKey("quiet_hours_enabled")
    private val QUIET_HOURS_START_KEY = intPreferencesKey("quiet_hours_start")
    private val QUIET_HOURS_END_KEY = intPreferencesKey("quiet_hours_end")
    
    private val TOTAL_TASKS_KEY = intPreferencesKey("total_tasks_completed")
    private val TOTAL_SESSIONS_KEY = intPreferencesKey("total_focus_sessions")
    private val TOTAL_FOCUS_MINS_KEY = intPreferencesKey("total_focus_minutes")
    private val TOTAL_GAMES_KEY = intPreferencesKey("total_games_played")
    private val BADGES_KEY = stringPreferencesKey("earned_badges")
    private val TUTORIAL_KEY = booleanPreferencesKey("tutorial_completed")

    val stateFlow: Flow<GamificationState> = dataStore.data.map { prefs ->
        GamificationState(
            isLoading = false,
            xp = prefs[XP_KEY] ?: 0,
            level = prefs[LEVEL_KEY] ?: 1,
            dopamineGold = prefs[DG_KEY] ?: 0,
            dailyFlowStreak = prefs[STREAK_KEY] ?: 0,
            isProfileSetup = prefs[PROFILE_SETUP_KEY] ?: false,
            tutorialCompleted = prefs[TUTORIAL_KEY] ?: false,
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
            mergeHighScore = prefs[MERGE_HIGH_SCORE_KEY] ?: 0,
            totalTasksCompleted = prefs[TOTAL_TASKS_KEY] ?: 0,
            totalFocusSessions = prefs[TOTAL_SESSIONS_KEY] ?: 0,
            totalFocusMinutes = prefs[TOTAL_FOCUS_MINS_KEY] ?: 0,
            totalGamesPlayed = prefs[TOTAL_GAMES_KEY] ?: 0,
            earnedBadgeIds = prefs[BADGES_KEY]?.split(",")
                ?.filter { it.isNotEmpty() }?.toSet() ?: emptySet(),
            focusCheckInEnabled = prefs[FOCUS_CHECKIN_ENABLED_KEY] ?: false,
            focusCheckInIntervalMinutes = prefs[FOCUS_CHECKIN_INTERVAL_KEY] ?: 60,
            focusCheckInStartHour = prefs[FOCUS_CHECKIN_START_KEY] ?: 9,
            focusCheckInEndHour = prefs[FOCUS_CHECKIN_END_KEY] ?: 18,
            taskNudgeEnabled = prefs[TASK_NUDGE_ENABLED_KEY] ?: false,
            taskNudgeHour = prefs[TASK_NUDGE_HOUR_KEY] ?: 14,
            streakProtectorEnabled = prefs[STREAK_PROTECTOR_ENABLED_KEY] ?: false,
            streakProtectorHour = prefs[STREAK_PROTECTOR_HOUR_KEY] ?: 16,
            medicationReminderEnabled = prefs[MED_REMINDER_ENABLED_KEY] ?: false,
            medicationReminderHour = prefs[MED_REMINDER_HOUR_KEY] ?: 8,
            medicationReminderMinute = prefs[MED_REMINDER_MINUTE_KEY] ?: 0,
            quietHoursEnabled = prefs[QUIET_HOURS_ENABLED_KEY] ?: false,
            quietHoursStartHour = prefs[QUIET_HOURS_START_KEY] ?: 22,
            quietHoursEndHour = prefs[QUIET_HOURS_END_KEY] ?: 8
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

    suspend fun recordTaskCompleted() {
        dataStore.edit { prefs ->
            prefs[TOTAL_TASKS_KEY] = (prefs[TOTAL_TASKS_KEY] ?: 0) + 1
        }
    }

    suspend fun recordFocusSession(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[TOTAL_SESSIONS_KEY] = (prefs[TOTAL_SESSIONS_KEY] ?: 0) + 1
            prefs[TOTAL_FOCUS_MINS_KEY] = (prefs[TOTAL_FOCUS_MINS_KEY] ?: 0) + minutes
        }
    }

    suspend fun recordGamePlayed() {
        dataStore.edit { prefs ->
            prefs[TOTAL_GAMES_KEY] = (prefs[TOTAL_GAMES_KEY] ?: 0) + 1
        }
    }

    // Returns the badge ID if newly earned, null otherwise
    suspend fun checkAndAwardBadges(): String? {
        val prefs = dataStore.data.first()
        val earned = prefs[BADGES_KEY]?.split(",")
            ?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()

        val tasks = prefs[TOTAL_TASKS_KEY] ?: 0
        val sessions = prefs[TOTAL_SESSIONS_KEY] ?: 0
        val focusMins = prefs[TOTAL_FOCUS_MINS_KEY] ?: 0
        val streak = prefs[STREAK_KEY] ?: 0
        val games = prefs[TOTAL_GAMES_KEY] ?: 0
        val level = prefs[LEVEL_KEY] ?: 1

        // Check each badge in order — return first newly earned one
        val newBadge = AvailableBadges.firstOrNull { badge ->
            !earned.contains(badge.id) && badge.isEarned(
                tasks, sessions, focusMins, streak, games, level)
        }

        if (newBadge != null) {
            earned.add(newBadge.id)
            dataStore.edit { p ->
                p[BADGES_KEY] = earned.joinToString(",")
            }
            // Grant DG bonus for earning badge
            addReward(xpReward = 0, dgReward = newBadge.dgReward)
        }

        return newBadge?.id
    }

    suspend fun resetProfile() {
        dataStore.edit { prefs ->
            prefs[PROFILE_SETUP_KEY] = false
            prefs[WIZARD_STEP_KEY] = 0
            prefs[TUTORIAL_KEY] = false
        }
    }

    suspend fun completeTutorial() {
        dataStore.edit { prefs ->
            prefs[TUTORIAL_KEY] = true
        }
    }

    suspend fun saveNotificationSettings(
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
        quietHoursEndHour: Int
    ) {
        dataStore.edit { prefs ->
            prefs[FOCUS_CHECKIN_ENABLED_KEY] = focusCheckInEnabled
            prefs[FOCUS_CHECKIN_INTERVAL_KEY] = focusCheckInIntervalMinutes
            prefs[FOCUS_CHECKIN_START_KEY] = focusCheckInStartHour
            prefs[FOCUS_CHECKIN_END_KEY] = focusCheckInEndHour
            prefs[TASK_NUDGE_ENABLED_KEY] = taskNudgeEnabled
            prefs[TASK_NUDGE_HOUR_KEY] = taskNudgeHour
            prefs[STREAK_PROTECTOR_ENABLED_KEY] = streakProtectorEnabled
            prefs[STREAK_PROTECTOR_HOUR_KEY] = streakProtectorHour
            prefs[MED_REMINDER_ENABLED_KEY] = medicationReminderEnabled
            prefs[MED_REMINDER_HOUR_KEY] = medicationReminderHour
            prefs[MED_REMINDER_MINUTE_KEY] = medicationReminderMinute
            prefs[QUIET_HOURS_ENABLED_KEY] = quietHoursEnabled
            prefs[QUIET_HOURS_START_KEY] = quietHoursStartHour
            prefs[QUIET_HOURS_END_KEY] = quietHoursEndHour
        }
    }
}
