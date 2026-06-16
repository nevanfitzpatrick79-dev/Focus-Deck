package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@Composable
fun TutorialScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Track what the user has done in each step
    var anchorText by remember { mutableStateOf("") }
    var anchorSaved by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    var taskAdded by remember { mutableStateOf(false) }
    var timerStarted by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                repeat(totalSteps) { i ->
                    Surface(
                        modifier = Modifier.size(if (i == step) 10.dp else 8.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (i <= step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ) {}
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                modifier = Modifier.weight(1f),
                label = "tutorial_step"
            ) { currentStep ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (currentStep) {

                        // ── STEP 0: Working Memory Anchor ─────────────
                        0 -> {
                            Text("⚓", fontSize = 56.sp)
                            Text(
                                "What are you doing right now?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "The Working Memory Anchor at the top of your dashboard " +
                                "is the most important tool in this app.\n\n" +
                                "ADHD brains lose track of current tasks easily. " +
                                "Write what you're doing, and it stays visible.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                "Try it now — type something you're currently working on:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            OutlinedTextField(
                                value = anchorText,
                                onValueChange = {
                                    anchorText = it
                                    anchorSaved = false
                                },
                                placeholder = { Text("e.g. Setting up Focus Deck") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (anchorText.isNotBlank()) {
                                        viewModel.updateWorkingMemory(anchorText)
                                        anchorSaved = true
                                        keyboardController?.hide()
                                    }
                                })
                            )
                            if (!anchorSaved) {
                                Button(
                                    onClick = {
                                        if (anchorText.isNotBlank()) {
                                            viewModel.updateWorkingMemory(anchorText)
                                            anchorSaved = true
                                            keyboardController?.hide()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = anchorText.isNotBlank()
                                ) {
                                    Text("Save Anchor", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme
                                            .primaryContainer.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✓", color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        Text("Anchor saved. You'll see it every time " +
                                            "you open the app.",
                                            fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // ── STEP 1: Add a Task ────────────────────────
                        1 -> {
                            Text("📋", fontSize = 56.sp)
                            Text(
                                "Add your first task",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Tasks are the backbone of the app. " +
                                "Add anything — big or small. " +
                                "Completing tasks earns XP and Dopamine Gold.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                "Add a task now:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            OutlinedTextField(
                                value = taskText,
                                onValueChange = {
                                    taskText = it
                                    taskAdded = false
                                },
                                placeholder = { Text("e.g. Check my emails") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (taskText.isNotBlank()) {
                                        viewModel.addTutorialTask(taskText)
                                        taskAdded = true
                                        keyboardController?.hide()
                                    }
                                }),
                                enabled = !taskAdded
                            )
                            if (!taskAdded) {
                                Button(
                                    onClick = {
                                        if (viewModel.addTutorialTask(taskText)) {
                                            taskAdded = true
                                            keyboardController?.hide()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = taskText.isNotBlank()
                                ) {
                                    Text("Add Task", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme
                                            .primaryContainer.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✓", color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        Text("Task added. You'll find it on your dashboard. " +
                                            "Tap the checkbox to complete it.",
                                            fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // ── STEP 2: Focus Timer ───────────────────────
                        2 -> {
                            Text("⏱", fontSize = 56.sp)
                            Text(
                                "The Focus Timer",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Work in timed sessions to help your brain " +
                                "commit to a start and end point.\n\n" +
                                "When the timer finishes you earn XP and " +
                                "Dopamine Gold. Stopping early doesn't — " +
                                "so try to run it to completion.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                "On your dashboard, set a timer and start it. " +
                                "You don't need to finish it now — just try starting one.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme
                                        .secondaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("💡 Tip", fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Your preferred focus duration from setup is " +
                                        "highlighted as the default. You can always change it.",
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                            // User doesn't have to do this in the tutorial screen itself
                            // — just explain and let them proceed
                        }

                        // ── STEP 3: Rewards ───────────────────────────
                        3 -> {
                            Text("💎", fontSize = 56.sp)
                            Text(
                                "Dopamine Gold",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Every task completed, timer finished, and game played " +
                                "earns Dopamine Gold.\n\n" +
                                "Spend it in the Reward Shop on real breaks, " +
                                "themes, titles, and games.\n\n" +
                                "The shop isn't a distraction — it's part of the system. " +
                                "Scheduled rewards make it easier to start hard tasks.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme
                                        .primaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("You're ready.", fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Your dashboard is waiting. " +
                                        "The anchor is set. The first task is added.\n" +
                                        "Start small. Build from there.",
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Back") }
                }

                val canProceed = when (step) {
                    0 -> anchorSaved
                    1 -> taskAdded
                    else -> true
                }

                Button(
                    onClick = {
                        if (step < totalSteps - 1) {
                            step++
                        } else {
                            viewModel.completeTutorial()
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = canProceed
                ) {
                    Text(
                        when {
                            step == 0 && !anchorSaved -> "Save anchor first"
                            step == 1 && !taskAdded -> "Add a task first"
                            step == totalSteps - 1 -> "Go to Dashboard 🚀"
                            else -> "Next"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
