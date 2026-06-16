package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    val state by viewModel.gamificationState.collectAsState()
    val context = LocalContext.current

    // Local mutable state for the notification form
    var focusEnabled by remember { mutableStateOf(state.focusCheckInEnabled) }
    var focusInterval by remember { mutableStateOf(state.focusCheckInIntervalMinutes) }
    var focusStart by remember { mutableStateOf(state.focusCheckInStartHour) }
    var focusEnd by remember { mutableStateOf(state.focusCheckInEndHour) }
    var taskNudge by remember { mutableStateOf(state.taskNudgeEnabled) }
    var taskNudgeHour by remember { mutableStateOf(state.taskNudgeHour) }
    var streakProtector by remember { mutableStateOf(state.streakProtectorEnabled) }
    var streakHour by remember { mutableStateOf(state.streakProtectorHour) }
    var medReminder by remember { mutableStateOf(state.medicationReminderEnabled) }
    var medHour by remember { mutableStateOf(state.medicationReminderHour) }
    var medMinute by remember { mutableStateOf(state.medicationReminderMinute) }
    var quietHours by remember { mutableStateOf(state.quietHoursEnabled) }
    var quietStart by remember { mutableStateOf(state.quietHoursStartHour) }
    var quietEnd by remember { mutableStateOf(state.quietHoursEndHour) }
    var showResetDialog by remember { mutableStateOf(false) }
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotifPermission = granted }

    fun saveAll() {
        viewModel.saveNotificationSettings(
            focusEnabled, focusInterval, focusStart, focusEnd,
            taskNudge, taskNudgeHour,
            streakProtector, streakHour,
            medReminder, medHour, medMinute,
            quietHours, quietStart, quietEnd,
            context
        )
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Profile?") },
            text = { Text("This will return you to the setup wizard. Your tasks and progress won't be affected.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProfile(context)
                    showResetDialog = false
                    onEditProfile()
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {

            // Profile section
            item { SettingsSectionHeader("Profile") }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your name, focus time, and preferences",
                        onClick = onEditProfile
                    )
                    HorizontalDivider()
                    SettingsRow(
                        icon = Icons.Default.Refresh,
                        title = "Reset Profile",
                        subtitle = "Return to setup wizard",
                        onClick = { showResetDialog = true },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Notifications section
            item { SettingsSectionHeader("Reminders") }

            // Permission banner
            if (!hasNotifPermission) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Notifications are disabled",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("Tap to allow Focus Deck to send reminders.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                        .copy(alpha = 0.8f))
                            }
                            TextButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }) { Text("Enable") }
                        }
                    }
                }
            }

            // Focus Check-In
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Focus Check-In 🎯",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "\"What are you working on right now?\" at regular intervals during your active hours.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = focusEnabled,
                            onCheckedChange = {
                                focusEnabled = it
                                saveAll()
                            },
                            enabled = hasNotifPermission
                        )
                    }
                    if (focusEnabled) {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            IntervalPicker("Every", focusInterval,
                                listOf(30, 45, 60, 90, 120)) {
                                focusInterval = it; saveAll()
                            }
                            HourRangePicker(
                                "Active hours",
                                focusStart, focusEnd,
                                onStartChange = { focusStart = it; saveAll() },
                                onEndChange = { focusEnd = it; saveAll() }
                            )
                        }
                    }
                }
            }

            // Task Nudge
            item {
                SettingsCard {
                    NotifToggleRow(
                        emoji = "📋",
                        title = "Task Nudge",
                        subtitle = "Gentle reminder if you haven't completed a task today.",
                        enabled = taskNudge,
                        hasPermission = hasNotifPermission,
                        onToggle = { taskNudge = it; saveAll() }
                    )
                    if (taskNudge) {
                        HorizontalDivider()
                        HourPicker("Remind me at", taskNudgeHour,
                            modifier = Modifier.padding(16.dp)) {
                            taskNudgeHour = it; saveAll()
                        }
                    }
                }
            }

            // Streak Protector
            item {
                SettingsCard {
                    NotifToggleRow(
                        emoji = "🔥",
                        title = "Streak Protector",
                        subtitle = "Reminds you to check in before your streak resets.",
                        enabled = streakProtector,
                        hasPermission = hasNotifPermission,
                        onToggle = { streakProtector = it; saveAll() }
                    )
                    if (streakProtector) {
                        HorizontalDivider()
                        HourPicker("Remind me at", streakHour,
                            modifier = Modifier.padding(16.dp)) {
                            streakHour = it; saveAll()
                        }
                    }
                }
            }

            // Medication Reminder — only show if profile says they take meds
            if (state.takesMedication == "Yes") {
                item {
                    SettingsCard {
                        NotifToggleRow(
                            emoji = "💊",
                            title = "Medication Reminder",
                            subtitle = "Daily reminder at a set time.",
                            enabled = medReminder,
                            hasPermission = hasNotifPermission,
                            onToggle = { medReminder = it; saveAll() }
                        )
                        if (medReminder) {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HourPicker("Hour", medHour,
                                    modifier = Modifier.weight(1f)) {
                                    medHour = it; saveAll()
                                }
                                MinutePicker("Minute", medMinute,
                                    modifier = Modifier.weight(1f)) {
                                    medMinute = it; saveAll()
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            Text(
                                "This reminder is a convenience tool only. Always follow your " +
                                "prescriber's instructions regarding medication timing and dosage.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Quiet Hours
            item {
                SettingsCard {
                    NotifToggleRow(
                        emoji = "🌙",
                        title = "Quiet Hours",
                        subtitle = "No reminders during these hours (except medication).",
                        enabled = quietHours,
                        hasPermission = hasNotifPermission,
                        onToggle = { quietHours = it; saveAll() }
                    )
                    if (quietHours) {
                        HorizontalDivider()
                        HourRangePicker(
                            "Quiet from",
                            quietStart, quietEnd,
                            onStartChange = { quietStart = it; saveAll() },
                            onEndChange = { quietEnd = it; saveAll() },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // App info
            item { SettingsSectionHeader("About") }
            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Focus Deck", fontWeight = FontWeight.Bold)
                        Text(
                            "Built for ADHD brains. All data stays on your device.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This app provides general productivity and wellbeing " +
                            "suggestions only. It is not a medical device and does not " +
                            "provide medical, psychological, nutritional, or therapeutic " +
                            "advice. Always consult a qualified professional for health " +
                            "concerns.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        content = { Column(content = content) }
    )
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint,
            modifier = Modifier.padding(end = 14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}

@Composable
fun NotifToggleRow(
    emoji: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    hasPermission: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("$emoji $title", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp))
        }
        Switch(checked = enabled, onCheckedChange = onToggle, enabled = hasPermission)
    }
}

@Composable
fun IntervalPicker(
    label: String,
    current: Int,
    options: List<Int>,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt ->
                FilterChip(
                    selected = current == opt,
                    onClick = { onSelect(opt) },
                    label = { Text("${opt}m", fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
fun HourPicker(
    label: String,
    current: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(-1, 1).forEach { delta ->
                OutlinedButton(
                    onClick = {
                        onSelect(((current + delta + 24) % 24))
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (delta < 0) "−" else "+")
                }
            }
            Text(
                formatHour(current),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun MinutePicker(
    label: String,
    current: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(-15, 15).forEach { delta ->
                OutlinedButton(
                    onClick = { onSelect(((current + delta + 60) % 60)) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (delta < 0) "−" else "+")
                }
            }
            Text(
                "%02d".format(current),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun HourRangePicker(
    label: String,
    startHour: Int,
    endHour: Int,
    onStartChange: (Int) -> Unit,
    onEndChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HourPicker("From", startHour, modifier = Modifier.weight(1f), onSelect = onStartChange)
            Text("→", fontSize = 18.sp)
            HourPicker("To", endHour, modifier = Modifier.weight(1f), onSelect = onEndChange)
        }
    }
}

fun formatHour(hour: Int): String {
    val h = hour % 12
    val amPm = if (hour < 12) "AM" else "PM"
    return "${if (h == 0) 12 else h} $amPm"
}
