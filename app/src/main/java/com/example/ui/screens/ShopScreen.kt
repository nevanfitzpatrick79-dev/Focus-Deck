package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CustomSnackbarOverlay
import com.example.ui.components.ThemeBackgroundLayer
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val gamificationState by viewModel.gamificationState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundLayer(themeName = gamificationState.currentTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Reward Shop") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Text(
                            text = "💎 ${gamificationState.dopamineGold} DG",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp),
                            color = Color(0xFFFFD700)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Themes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                item { ThemeShopCard("Cosmic Slate", 0, gamificationState.unlockedThemes.contains("Cosmic Slate"), gamificationState.currentTheme == "Cosmic Slate") { viewModel.buyTheme("Cosmic Slate", 0) } }
                item { ThemeShopCard("Forest Sanctuary", 100, gamificationState.unlockedThemes.contains("Forest Sanctuary"), gamificationState.currentTheme == "Forest Sanctuary") { viewModel.buyTheme("Forest Sanctuary", 100) } }
                item { ThemeShopCard("Cyber Oasis", 250, gamificationState.unlockedThemes.contains("Cyber Oasis"), gamificationState.currentTheme == "Cyber Oasis") { viewModel.buyTheme("Cyber Oasis", 250) } }
            }
        }
        
        CustomSnackbarOverlay(message = snackbarMessage)
    }
}

@Composable
fun ThemeShopCard(
    themeName: String,
    cost: Int,
    isUnlocked: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = themeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if(!isUnlocked) {
                 Text(text = "$cost DG", color = Color(0xFFFFD700))
            } else if (isActive) {
                 Text(text = "Active", color = MaterialTheme.colorScheme.primary)
            } else {
                 Text(text = "Unlocked", color = Color.Gray)
            }
        }
        
        if (!isActive) {
            Button(onClick = onClick) {
                Text(if (isUnlocked) "Apply" else "Buy")
            }
        }
    }
}
