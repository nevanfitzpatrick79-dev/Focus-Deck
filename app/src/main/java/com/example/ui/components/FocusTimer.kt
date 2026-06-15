package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.TimerState

@Composable
fun FocusTimer(
    state: TimerState,
    onStart: (Int) -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = state.progress,
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
             Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = { onStart(5) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    Text("5m", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                FloatingActionButton(
                    onClick = { onStart(25) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, "Start 25m Timer")
                }
            }
        }
    }
}
