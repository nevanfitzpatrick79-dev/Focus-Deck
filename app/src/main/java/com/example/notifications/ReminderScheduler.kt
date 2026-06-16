package com.example.notifications

import android.content.Context
import androidx.work.*
import com.example.data.GamificationState
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun rescheduleAll(context: Context, state: GamificationState) {
        val workManager = WorkManager.getInstance(context)

        // Cancel all existing reminders before rescheduling
        workManager.cancelAllWorkByTag("focus_deck_reminders")

        scheduleFocusCheckIn(context, state)
        scheduleTaskNudge(context, state)
        scheduleStreakProtector(context, state)
        scheduleMedicationReminder(context, state)
    }

    private fun scheduleFocusCheckIn(context: Context, state: GamificationState) {
        if (!state.focusCheckInEnabled) return

        val intervalMs = state.focusCheckInIntervalMinutes.toLong() * 60 * 1000
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            state.focusCheckInIntervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setInputData(workDataOf(ReminderWorker.KEY_TYPE to ReminderWorker.TYPE_FOCUS))
            .addTag("focus_deck_reminders")
            .addTag("focus_checkin")
            .setInitialDelay(minutesUntilNextActiveHour(
                state.focusCheckInStartHour, state.focusCheckInIntervalMinutes),
                TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "focus_checkin",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun scheduleTaskNudge(context: Context, state: GamificationState) {
        if (!state.taskNudgeEnabled) return
        scheduleDailyAt(context, state.taskNudgeHour, 0,
            ReminderWorker.TYPE_TASK, "task_nudge")
    }

    private fun scheduleStreakProtector(context: Context, state: GamificationState) {
        if (!state.streakProtectorEnabled) return
        scheduleDailyAt(context, state.streakProtectorHour, 0,
            ReminderWorker.TYPE_STREAK, "streak_protector")
    }

    private fun scheduleMedicationReminder(context: Context, state: GamificationState) {
        if (!state.medicationReminderEnabled) return
        scheduleDailyAt(context, state.medicationReminderHour,
            state.medicationReminderMinute,
            ReminderWorker.TYPE_MED, "med_reminder")
    }

    private fun scheduleDailyAt(
        context: Context,
        hour: Int,
        minute: Int,
        type: String,
        uniqueName: String
    ) {
        val delay = minutesUntilTime(hour, minute)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf(ReminderWorker.KEY_TYPE to type))
            .addTag("focus_deck_reminders")
            .addTag(uniqueName)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun minutesUntilTime(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return (target.timeInMillis - now.timeInMillis) / 60_000
    }

    private fun minutesUntilNextActiveHour(startHour: Int, intervalMinutes: Int): Long {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        return if (currentHour < startHour) {
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, 0)
            }
            (target.timeInMillis - now.timeInMillis) / 60_000
        } else {
            intervalMinutes.toLong()
        }
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("focus_deck_reminders")
    }
}
