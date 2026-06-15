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
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gamification_state")

data class GamificationState(
    val isLoading: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val dopamineGold: Int = 0,
    val dailyFlowStreak: Int = 0,
    val isProfileSetup: Boolean = false,
    val currentTheme: String = "Cosmic Slate",
    val unlockedThemes: Set<String> = setOf("Cosmic Slate")
)

class GamificationRepository(private val dataStore: DataStore<Preferences>) {
    private val XP_KEY = intPreferencesKey("xp")
    private val LEVEL_KEY = intPreferencesKey("level")
    private val DG_KEY = intPreferencesKey("dopamine_gold")
    private val STREAK_KEY = intPreferencesKey("daily_streak")
    private val PROFILE_SETUP_KEY = booleanPreferencesKey("profile_setup")
    private val THEME_KEY = stringPreferencesKey("current_theme")
    // Use comma separated for simple set emulation
    private val UNLOCKED_THEMES_KEY = stringPreferencesKey("unlocked_themes")
    
    val stateFlow: Flow<GamificationState> = dataStore.data.map { prefs ->
        GamificationState(
            isLoading = false,
            xp = prefs[XP_KEY] ?: 0,
            level = prefs[LEVEL_KEY] ?: 1,
            dopamineGold = prefs[DG_KEY] ?: 0,
            dailyFlowStreak = prefs[STREAK_KEY] ?: 0,
            isProfileSetup = prefs[PROFILE_SETUP_KEY] ?: false,
            currentTheme = prefs[THEME_KEY] ?: "Cosmic Slate",
            unlockedThemes = prefs[UNLOCKED_THEMES_KEY]?.split(",")?.toSet() ?: setOf("Cosmic Slate")
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

    suspend fun setProfileSetupComplete() {
        dataStore.edit { prefs ->
            prefs[PROFILE_SETUP_KEY] = true
        }
    }
}
