package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskBoard(
    tasks: List<Task>,
    selectedCategory: TaskCategory,
    enabledCategories: Set<String>,
    onCategorySelected: (TaskCategory) -> Unit,
    onAddTask: (String, TaskPriority, TaskCategory, Long?) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedAddCategory by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var showCompleted by remember { mutableStateOf(false) }
    var showBrainDump by remember { mutableStateOf(false) }
    var brainDumpText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    Column(modifier = modifier.fillMaxWidth()) {

        // Category filter strip
        val visibleCategories = TaskCategory.entries.filter { cat ->
            cat == TaskCategory.ALL || enabledCategories.contains(cat.name)
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visibleCategories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelected(cat) },
                    label = { Text("${cat.emoji} ${cat.label}", fontSize = 12.sp) }
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // Header row with brain dump button
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { showBrainDump = !showBrainDump }) {
                    Icon(Icons.Default.Bolt, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Brain Dump", fontSize = 12.sp)
                }
            }

            // Brain dump mode — fast capture, no options
            AnimatedVisibility(visible = showBrainDump) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚡ Brain Dump — capture first, organise later",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "One thought per line. Press Enter after each.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = brainDumpText,
                            onValueChange = { brainDumpText = it },
                            placeholder = { Text("What's on your mind?") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 6
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                brainDumpText = ""
                                showBrainDump = false
                            }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                brainDumpText.lines()
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .forEach { line ->
                                        onAddTask(line, TaskPriority.MEDIUM,
                                            TaskCategory.PERSONAL, null)
                                    }
                                brainDumpText = ""
                                showBrainDump = false
                            }) { Text("Capture All") }
                        }
                    }
                }
            }

            // Standard add row
            if (!showBrainDump) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("What needs doing?") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onAddTask(newTaskTitle, selectedPriority,
                                selectedAddCategory, null)
                            newTaskTitle = ""
                            keyboardController?.hide()
                        })
                    )
                    IconButton(onClick = {
                        onAddTask(newTaskTitle, selectedPriority,
                            selectedAddCategory, null)
                        newTaskTitle = ""
                        keyboardController?.hide()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }

                // Priority + category selectors
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("P:", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = selectedPriority == p,
                            onClick = { selectedPriority = p },
                            label = { Text(p.name.take(3), fontSize = 11.sp) },
                            leadingIcon = {
                                Box(Modifier.size(6.dp).background(
                                    priorityColor(p), CircleShape))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cat:", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    // Show only non-ALL categories in add row
                    TaskCategory.entries
                        .filter { it != TaskCategory.ALL && enabledCategories.contains(it.name) }
                        .take(4)
                        .forEach { cat ->
                        FilterChip(
                            selected = selectedAddCategory == cat,
                            onClick = { selectedAddCategory = cat },
                            label = { Text(cat.emoji, fontSize = 14.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (activeTasks.isEmpty()) {
                    item {
                        Text(
                            if (selectedCategory == TaskCategory.ALL)
                                "No active tasks. Add one above! 🎯"
                            else
                                "No ${selectedCategory.label} tasks. Add one above!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(activeTasks, key = { it.id }) { task ->
                        TaskItem(task, onToggleTask, onDeleteTask)
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCompleted = !showCompleted }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Completed (${completedTasks.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Icon(
                                if (showCompleted) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (showCompleted) {
                        items(completedTasks, key = { it.id }) { task ->
                            TaskItem(task, onToggleTask, onDeleteTask)
                        }
                    }
                }
            }
        }
    }
}

fun priorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH   -> Color(0xFFE53935)
    TaskPriority.MEDIUM -> Color(0xFFFB8C00)
    TaskPriority.LOW    -> Color(0xFF43A047)
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    val priority = task.priorityEnum()
    val isOverdue = task.isOverdue()
    val isDueToday = task.isDueToday()

    val cardColor = when {
        task.isCompleted -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        isOverdue -> Color(0xFFE53935).copy(alpha = 0.08f)
        isDueToday -> Color(0xFFFB8C00).copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle(task) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!task.isCompleted) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(priorityColor(priority), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) }
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface
                )
                // Due date and category chips
                if (!task.isCompleted) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Category badge
                        val cat = task.categoryEnum()
                        if (cat != TaskCategory.PERSONAL) {
                            Text(
                                "${cat.emoji} ${cat.label}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        // Due date badge
                        task.dueDateMs?.let { dueMs ->
                            val label = when {
                                isOverdue -> "⚠ Overdue"
                                isDueToday -> "📅 Today"
                                else -> "📅 ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(dueMs))}"
                            }
                            Text(
                                label,
                                fontSize = 10.sp,
                                color = when {
                                    isOverdue -> Color(0xFFE53935)
                                    isDueToday -> Color(0xFFFB8C00)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                },
                                fontWeight = if (isOverdue || isDueToday)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
