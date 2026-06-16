package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.TimerState

@Composable
fun FocusTimer(
    state: TimerState,
    preferredMinutes: Int = 25,
    onStart: (Int) -> Unit,
    onStop: () -> Unit
) {
    var customMinutes by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.size(150.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                strokeWidth = 12.dp
            )
            
            Text(
                text = if(state.initialSeconds == 0) "25:00" else state.displayString,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isRunning) {
            FloatingActionButton(
                onClick = onStop,
                containerColor = MaterialTheme.colorScheme.error,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Stop, "Stop Timer")
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick-start preset row
                val presets = listOf(5, 15, preferredMinutes, 50)
                    .distinct()
                    .sorted()

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { mins ->
                        val isPreferred = mins == preferredMinutes
                        if (isPreferred) {
                            Button(
                                onClick = { onStart(mins) },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("$mins min ⭐", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            AssistChip(
                                onClick = { onStart(mins) },
                                label = { Text("$mins min") }
                            )
                        }
                    }
                }
        
                // Custom duration row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) customMinutes = it },
                        placeholder = { Text("min") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                val mins = customMinutes.toIntOrNull()
                                if (mins != null && mins > 0) {
                                    onStart(mins)
                                    customMinutes = ""
                                }
                            }
                        )
                    )
                    Button(
                        onClick = {
                            val mins = customMinutes.toIntOrNull()
                            if (mins != null && mins > 0) {
                                onStart(mins)
                                customMinutes = ""
                            }
                        },
                        enabled = customMinutes.toIntOrNull()?.let { it > 0 } == true
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start custom timer")
                    }
                }
            }
        }
    }
}
