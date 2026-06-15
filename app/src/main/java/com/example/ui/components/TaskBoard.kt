package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.Task

@Composable
fun TaskBoard(
    tasks: List<Task>,
    onAddTask: (String) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTaskTitle by remember { mutableStateOf("") }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Active Tasks",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
             androidx.compose.material3.OutlinedTextField(
                 value = newTaskTitle,
                 onValueChange = { newTaskTitle = it },
                 placeholder = { Text("What needs getting done?") },
                 modifier = Modifier.weight(1f),
                 singleLine = true,
                 keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                 keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { 
                     onAddTask(newTaskTitle)
                     newTaskTitle = ""
                     keyboardController?.hide()
                 })
             )
             IconButton(onClick = { 
                 onAddTask(newTaskTitle)
                 newTaskTitle = ""
                 keyboardController?.hide()
             }) {
                 Icon(Icons.Default.Add, contentDescription = "Add Task")
             }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tasks, key = { it.id }) { task ->
                AnimatedVisibility(visible = true) {
                    TaskItem(task, onToggleTask, onDeleteTask)
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle(task) },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                 MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) 
                 else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) }
            )
            Text(
                text = task.title,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if(task.isCompleted) TextDecoration.LineThrough else null,
                color = if(task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { onDelete(task) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
