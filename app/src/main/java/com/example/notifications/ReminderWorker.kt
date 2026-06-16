package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.dataStore
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TYPE = "reminder_type"
        const val TYPE_FOCUS = "focus"
        const val TYPE_TASK = "task"
        const val TYPE_STREAK = "streak"
        const val TYPE_MED = "medication"
    }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.dataStore.data.first()

        // Check quiet hours before firing any notification
        val quietEnabled = prefs[
            androidx.datastore.preferences.core.booleanPreferencesKey("quiet_hours_enabled")
        ] ?: false
        val quietStart = prefs[
            androidx.datastore.preferences.core.intPreferencesKey("quiet_hours_start")
        ] ?: 22
        val quietEnd = prefs[
            androidx.datastore.preferences.core.intPreferencesKey("quiet_hours_end")
        ] ?: 8

        // Medication reminder ignores quiet hours — it's important
        val type = inputData.getString(KEY_TYPE) ?: return Result.success()
        if (type != TYPE_MED &&
            NotificationHelper.isInQuietHours(quietEnabled, quietStart, quietEnd)) {
            return Result.success()
        }

        val streak = prefs[
            androidx.datastore.preferences.core.intPreferencesKey("daily_flow_streak")
        ] ?: 0

        when (type) {
            TYPE_FOCUS -> {
                val enabled = prefs[
                    androidx.datastore.preferences.core.booleanPreferencesKey("focus_checkin_enabled")
                ] ?: false
                if (enabled) NotificationHelper.showFocusCheckIn(applicationContext)
            }
            TYPE_TASK -> {
                val enabled = prefs[
                    androidx.datastore.preferences.core.booleanPreferencesKey("task_nudge_enabled")
                ] ?: false
                if (enabled) NotificationHelper.showTaskNudge(applicationContext)
            }
            TYPE_STREAK -> {
                val enabled = prefs[
                    androidx.datastore.preferences.core.booleanPreferencesKey("streak_protector_enabled")
                ] ?: false
                if (enabled) NotificationHelper.showStreakProtector(applicationContext, streak)
            }
            TYPE_MED -> {
                val enabled = prefs[
                    androidx.datastore.preferences.core.booleanPreferencesKey("med_reminder_enabled")
                ] ?: false
                if (enabled) NotificationHelper.showMedicationReminder(applicationContext)
            }
        }
        return Result.success()
    }
}
