package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_FOCUS = "focus_checkin"
    const val CHANNEL_TASKS = "task_nudge"
    const val CHANNEL_STREAK = "streak_protector"
    const val CHANNEL_MED = "medication_reminder"

    const val NOTIF_ID_FOCUS = 1001
    const val NOTIF_ID_TASK = 1002
    const val NOTIF_ID_STREAK = 1003
    const val NOTIF_ID_MED = 1004

    fun createChannels(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

            listOf(
                NotificationChannel(CHANNEL_FOCUS, "Focus Check-In",
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Gentle reminders to check in on what you're working on"
                },
                NotificationChannel(CHANNEL_TASKS, "Task Nudge",
                    NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Reminder to complete at least one task today"
                },
                NotificationChannel(CHANNEL_STREAK, "Streak Protector",
                    NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Reminds you to check in before your streak resets"
                },
                NotificationChannel(CHANNEL_MED, "Medication Reminder",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Daily medication reminder"
                }
            ).forEach { manager.createNotificationChannel(it) }
        }
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_anchor", true)
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showFocusCheckIn(context: Context) {
        val messages = listOf(
            "What are you working on right now?",
            "Quick check — still on track?",
            "Hey. What's the current mission?",
            "Gentle nudge — what's in focus?",
            "Time for a quick check-in."
        )
        show(context, CHANNEL_FOCUS, NOTIF_ID_FOCUS,
            "Focus Check-In \uD83C\uDFAF",
            messages.random(),
            openAppIntent(context))
    }

    fun showTaskNudge(context: Context) {
        show(context, CHANNEL_TASKS, NOTIF_ID_TASK,
            "Tasks waiting \uD83D\uDCCB",
            "You haven't completed a task yet today. Even a small one counts.",
            openAppIntent(context))
    }

    fun showStreakProtector(context: Context, streakDays: Int) {
        val msg = if (streakDays > 1)
            "Your $streakDays-day streak is still going. Check in to keep it."
        else
            "Check in today to start your streak."
        show(context, CHANNEL_STREAK, NOTIF_ID_STREAK,
            "Streak Protector \uD83D\uDD25", msg, openAppIntent(context))
    }

    fun showMedicationReminder(context: Context) {
        show(context, CHANNEL_MED, NOTIF_ID_MED,
            "Medication Reminder \uD83D\uDC8A",
            "Time for your medication.",
            openAppIntent(context))
    }

    private fun show(
        context: Context,
        channelId: String,
        notifId: Int,
        title: String,
        message: String,
        pendingIntent: PendingIntent
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(
                if (channelId == CHANNEL_MED) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        manager.notify(notifId, notification)
    }

    fun isInQuietHours(
        quietEnabled: Boolean,
        quietStart: Int,
        quietEnd: Int
    ): Boolean {
        if (!quietEnabled) return false
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (quietStart > quietEnd) {
            // Spans midnight e.g. 22:00 to 08:00
            hour >= quietStart || hour < quietEnd
        } else {
            hour in quietStart until quietEnd
        }
    }
}
