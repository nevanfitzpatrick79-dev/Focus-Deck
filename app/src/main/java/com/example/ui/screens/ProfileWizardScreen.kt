package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.viewmodel.MainViewModel
import androidx.compose.foundation.layout.RowScope

// Step labels for the progress header
private val STEP_TITLES = listOf(
    "Welcome",
    "Focus Time",
    "Energy",
    "Life Areas",
    "Your Brain",
    "Your Body",
    "Diet & Rewards",
    "Your Theme"
)

@Composable
fun ProfileWizardScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit
) {
    val gamificationState by viewModel.gamificationState.collectAsState()
    val totalSteps = STEP_TITLES.size
    var step by remember { mutableStateOf(gamificationState.wizardStep) }

    // Wizard state — basic profile
    var userName by remember { mutableStateOf(gamificationState.userName.ifBlank { "" }) }
    var focusMinutes by remember { mutableStateOf(gamificationState.preferredFocusMinutes) }
    var peakEnergy by remember { mutableStateOf(gamificationState.peakEnergyTime.ifBlank { "Morning" }) }
    var enabledCategories by remember { mutableStateOf(
        gamificationState.enabledCategories.ifEmpty {
            setOf("WORK","HOME","PERSONAL","ERRANDS","HEALTH")
        })
    }
    // Health profile
    var adhdPresentation by remember { mutableStateOf("") }
    var coOccurring by remember { mutableStateOf(setOf<String>()) }
    var takesMedication by remember { mutableStateOf("") }
    // Physical profile
    var activityLevel by remember { mutableStateOf("") }
    var physicalLimitations by remember { mutableStateOf(setOf<String>()) }
    // Diet & rewards
    var dietaryRestrictions by remember { mutableStateOf(setOf<String>()) }
    var rewardPreferences by remember { mutableStateOf(
        listOf("Games","Creative","Movement","Rest","Music","Social")
    )}
    // Theme
    var selectedTheme by remember { mutableStateOf(gamificationState.currentTheme) }

    fun advance() { step = (step + 1).also { viewModel.saveWizardStep(it) } }
    fun retreat() { step = (step - 1).also { viewModel.saveWizardStep(it) } }
    fun skipToNext() { advance() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step indicator
            Text(
                STEP_TITLES[step],
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (step + 1).toFloat() / totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
            Text(
                "${step + 1} / $totalSteps",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState)
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    else
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                },
                modifier = Modifier.weight(1f),
                label = "wizard_step"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {

                        // ── STEP 0: Welcome + Name ──────────────────────────
                        0 -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("👋", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Welcome to Focus Deck",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "A focus and life management system built around how ADHD brains actually work.\n\nNo shame. No judgment. Just tools that fit the way you think.",
                                textAlign = TextAlign.Center, fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                            Spacer(modifier = Modifier.height(28.dp))
                            Text("What should we call you?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                placeholder = { Text("Your name or nickname") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge)
                        }

                        // ── STEP 1: Focus Duration ─────────────────────────
                        1 -> {
                            Text("⏱", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("How long can you focus?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Be honest with yourself. A 10-minute session you actually do beats a 90-minute one you avoid.",
                                textAlign = TextAlign.Center, fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(20.dp))
                            listOf(
                                Triple(5, "5 minutes", "Just a start — that's enough"),
                                Triple(10, "10 minutes", "A solid short burst"),
                                Triple(15, "15 minutes", "Good for most days"),
                                Triple(25, "25 minutes", "Classic Pomodoro"),
                                Triple(50, "50 minutes", "Deep work mode")
                            ).forEach { (mins, label, sub) ->
                                WizardSelectCard(
                                    selected = focusMinutes == mins,
                                    onClick = { focusMinutes = mins }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(sub, fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        if (focusMinutes == mins) {
                                            Text("✓", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        // ── STEP 2: Peak Energy ────────────────────────────
                        2 -> {
                            Text("☀️", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("When does your brain work best?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("We'll suggest tackling high-priority tasks during your peak time.",
                                textAlign = TextAlign.Center, fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(20.dp))
                            listOf(
                                Triple("Morning", "🌅", "Before noon — clear head, fresh start"),
                                Triple("Afternoon", "☀️", "12pm – 5pm — warmed up and rolling"),
                                Triple("Evening", "🌙", "After 5pm — quiet, fewer interruptions"),
                                Triple("Variable", "🔀", "Changes day to day — that's valid too")
                            ).forEach { (time, emoji, sub) ->
                                WizardSelectCard(
                                    selected = peakEnergy == time,
                                    onClick = { peakEnergy = time }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text(emoji, fontSize = 22.sp,
                                            modifier = Modifier.padding(end = 12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(time, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(sub, fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        if (peakEnergy == time) {
                                            Text("✓", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        // ── STEP 3: Life Areas ─────────────────────────────
                        3 -> {
                            Text("📋", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("What areas of life are you managing?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select what's relevant to you. You can change this later in Settings.",
                                textAlign = TextAlign.Center, fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(20.dp))
                            TaskCategory.entries.filter { it != TaskCategory.ALL }.forEach { cat ->
                                val isSelected = enabledCategories.contains(cat.name)
                                WizardSelectCard(
                                    selected = isSelected,
                                    onClick = {
                                        enabledCategories = if (isSelected)
                                            enabledCategories - cat.name
                                        else enabledCategories + cat.name
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(cat.emoji, fontSize = 22.sp,
                                                modifier = Modifier.padding(end = 10.dp))
                                            Text(cat.label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                    }
                                }
                            }
                        }

                        // ── STEP 4: ADHD Profile (Optional) ───────────────
                        4 -> {
                            OptionalSectionHeader(
                                emoji = "🧠",
                                title = "Your ADHD Profile",
                                subtitle = "Optional — this helps personalise tips and suggestions. All answers stay on your device.",
                                onSkip = { skipToNext() }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("How does your ADHD tend to show up?",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf(
                                Pair("Inattentive", "Distraction, mind-wandering, losing things"),
                                Pair("Hyperactive", "Restlessness, impulsivity, need to move"),
                                Pair("Combined", "A bit of both — shifts depending on the day"),
                                Pair("Not sure", "Still figuring it out"),
                                Pair("Prefer not to say", "That's okay")
                            ).forEach { (opt, sub) ->
                                WizardSelectCard(
                                    selected = adhdPresentation == opt,
                                    onClick = { adhdPresentation = opt }
                                ) {
                                    Column {
                                        Text(opt, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(sub, fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Does worry or nervousness affect your focus? (Optional)",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Select anything that resonates:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            listOf(
                                Pair("Anxiety", "Worry, overthinking, avoidance"),
                                Pair("Depression", "Low motivation, energy, or mood"),
                                Pair("Autism", "Sensory sensitivities, routine, social energy"),
                                Pair("Sleep issues", "Trouble falling asleep, staying asleep, or waking"),
                                Pair("Other", "Something else worth knowing")
                            ).forEach { (condition, sub) ->
                                val isSelected = coOccurring.contains(condition)
                                WizardSelectCard(
                                    selected = isSelected,
                                    onClick = {
                                        coOccurring = if (isSelected)
                                            coOccurring - condition
                                        else coOccurring + condition
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(condition, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(sub, fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Do you use medication for ADHD? (Optional)",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp))
                            listOf("Yes", "No", "Prefer not to say").forEach { opt ->
                                WizardSelectCard(
                                    selected = takesMedication == opt,
                                    onClick = { takesMedication = opt }
                                ) {
                                    Text(opt, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        // ── STEP 5: Physical Health (Optional) ────────────
                        5 -> {
                            OptionalSectionHeader(
                                emoji = "💪",
                                title = "Physical Profile",
                                subtitle = "Optional — helps filter break activities to ones that work for your body.",
                                onSkip = { skipToNext() }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("How active are you day-to-day?",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 8.dp))
                            listOf(
                                Pair("Sedentary", "Mostly seated, desk-based work"),
                                Pair("Light", "Some movement — occasional walks"),
                                Pair("Moderate", "Regular movement or exercise"),
                                Pair("Active", "Exercise is part of my daily routine"),
                                Pair("Prefer not to say", "")
                            ).forEach { (opt, sub) ->
                                WizardSelectCard(
                                    selected = activityLevel == opt,
                                    onClick = { activityLevel = opt }
                                ) {
                                    Column {
                                        Text(opt, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (sub.isNotBlank()) Text(sub, fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Any physical limitations we should know about? (Optional)",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Helps us avoid suggesting activities that won't work for you:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            listOf(
                                "Limited mobility",
                                "Chronic pain",
                                "Vision impairment",
                                "Hearing impairment"
                            ).forEach { limitation ->
                                val isSelected = physicalLimitations.contains(limitation)
                                WizardSelectCard(
                                    selected = isSelected,
                                    onClick = {
                                        physicalLimitations = if (isSelected)
                                            physicalLimitations - limitation
                                        else physicalLimitations + limitation
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(limitation, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                    }
                                }
                            }
                        }

                        // ── STEP 6: Diet & Reward Preferences ────────────
                        6 -> {
                            OptionalSectionHeader(
                                emoji = "🍎",
                                title = "Rewards & Diet",
                                subtitle = "Optional — personalises reward suggestions and filters out things that won't work for you.",
                                onSkip = { skipToNext() }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Any dietary restrictions?",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("We won't suggest food rewards that don't fit your needs:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            listOf(
                                Pair("Diabetic / Low sugar", "No dessert or sugar reward suggestions"),
                                Pair("Vegan", "Plant-based only"),
                                Pair("Vegetarian", "No meat"),
                                Pair("Gluten-free", "Celiac or sensitivity"),
                                Pair("Nut allergy", "No nut-based suggestions"),
                                Pair("Kosher", ""),
                                Pair("Halal", "")
                            ).forEach { (restriction, sub) ->
                                val key = restriction.substringBefore(" /").trim()
                                val isSelected = dietaryRestrictions.contains(key)
                                WizardSelectCard(
                                    selected = isSelected,
                                    onClick = {
                                        dietaryRestrictions = if (isSelected)
                                            dietaryRestrictions - key
                                        else dietaryRestrictions + key
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(restriction, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (sub.isNotBlank()) Text(sub, fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("What kinds of rewards motivate you most?",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tap to rank them — we'll prioritise these in the Shop:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            val rewardOptions = listOf(
                                Triple("Games", "🎮", "Mini-games and puzzles"),
                                Triple("Creative", "✏️", "Drawing, writing, making things"),
                                Triple("Movement", "🚶", "Physical breaks and stretches"),
                                Triple("Rest", "😴", "Quiet time, mindfulness"),
                                Triple("Music", "🎵", "Listening or playing"),
                                Triple("Social", "💬", "Connecting with someone")
                            )
                            rewardOptions.forEach { (pref, emoji, sub) ->
                                val rank = rewardPreferences.indexOf(pref)
                                    .takeIf { it >= 0 }?.plus(1)
                                WizardSelectCard(
                                    selected = rank != null && rank <= 3,
                                    onClick = {
                                        rewardPreferences = if (rewardPreferences.indexOf(pref) <= 2
                                            && rewardPreferences.size > 3) {
                                            // Move to end
                                            (rewardPreferences - pref) + pref
                                        } else {
                                            // Move to front
                                            listOf(pref) + (rewardPreferences - pref)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(emoji, fontSize = 22.sp,
                                                modifier = Modifier.padding(end = 10.dp))
                                            Column {
                                                Text(pref, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(sub, fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                        }
                                        if (rank != null) {
                                            Text(
                                                if (rank <= 3) "#$rank" else "",
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── STEP 7: Theme Selection ───────────────────────
                        7 -> {
                            Text("🎨", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Choose your theme",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("This is your space. Make it feel right.",
                                textAlign = TextAlign.Center, fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(20.dp))
                            listOf(
                                Triple("Cosmic Slate", "🌌", "Deep space — dark, focused, minimal"),
                                Triple("Forest Sanctuary", "🌿", "Calm greens — grounding, natural, soft"),
                                Triple("Cyber Oasis", "⚡", "Neon city — electric, sharp, energising")
                            ).forEach { (theme, emoji, desc) ->
                                WizardSelectCard(
                                    selected = selectedTheme == theme,
                                    onClick = { selectedTheme = theme }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 28.sp,
                                            modifier = Modifier.padding(end = 14.dp))
                                        Column {
                                            Text(theme, fontWeight = FontWeight.Black, fontSize = 16.sp,
                                                color = if (selectedTheme == theme)
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface)
                                            Text(desc, fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("You can unlock more themes in the Reward Shop later.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { retreat() },
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text("Back") }
            }
            Button(
                onClick = {
                    if (step < totalSteps - 1) {
                        advance()
                    } else {
                        viewModel.saveProfile(
                            name = userName.ifBlank { "Friend" },
                            focusMinutes = focusMinutes,
                            peakEnergy = peakEnergy,
                            enabledCategories = enabledCategories,
                            adhdPresentation = adhdPresentation,
                            coOccurring = coOccurring,
                            takesMedication = takesMedication,
                            activityLevel = activityLevel,
                            physicalLimitations = physicalLimitations,
                            dietaryRestrictions = dietaryRestrictions,
                            rewardPreferences = rewardPreferences,
                            selectedTheme = selectedTheme
                        )
                        viewModel.saveWizardStep(0)
                        onComplete()
                    }
                },
                modifier = Modifier.weight(if (step > 0) 1f else 1f).height(52.dp),
                enabled = step != 3 || enabledCategories.isNotEmpty()
            ) {
                Text(
                    when (step) {
                        totalSteps - 1 -> "Let's Go 🚀"
                        else -> "Next"
                    },
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }
        }
    }
}
}

// Reusable card for wizard selections
@Composable
fun WizardSelectCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

// Header for optional sections with skip button
@Composable
fun OptionalSectionHeader(
    emoji: String,
    title: String,
    subtitle: String,
    onSkip: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Text(subtitle, fontSize = 13.sp, textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        TextButton(onClick = onSkip) {
            Text("Skip this section →", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}
