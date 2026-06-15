package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AvailableBreakActivities
import com.example.data.AvailableTitles
import com.example.data.BreakActivity
import com.example.ui.components.CustomSnackbarOverlay
import com.example.ui.components.ThemeBackgroundLayer
import com.example.viewmodel.MainViewModel
import com.example.ui.games.MergeGameDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val gamificationState by viewModel.gamificationState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val activeBreak by viewModel.activeBreakActivity.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Breaks", "Games", "Themes", "Titles")

    // Break activity dialog
    activeBreak?.let { activity ->
        BreakActivityDialog(
            activity = activity,
            onDismiss = { viewModel.dismissBreakActivity() }
        )
    }

    val mergeGameState by viewModel.mergeGameState.collectAsState()
    mergeGameState?.let {
        MergeGameDialog(viewModel = viewModel)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundLayer(themeName = gamificationState.currentTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Reward Shop") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        Text(
                            "💎 ${gamificationState.dopamineGold} DG",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
                    tabs.forEachIndexed { i, title ->
                        Tab(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            text = { Text(title, fontSize = 13.sp) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            item {
                                Text(
                                    "Spend Dopamine Gold on a real break. " +
                                    "Breaks aren't cheating — they're the system working.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            items(AvailableBreakActivities) { activity ->
                                BreakActivityCard(
                                    activity = activity,
                                    canAfford = gamificationState.dopamineGold >= activity.cost,
                                    onClick = { viewModel.startBreakActivity(activity) }
                                )
                            }
                        }
                        1 -> {
                            // Games tab
                            item {
                                Text(
                                    "Spend a few minutes on a game. You earned this.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            item {
                                val gameState by viewModel.mergeGameState.collectAsState()
                                val hasSavedGame = false  // simplified — could check DataStore
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔮", fontSize = 40.sp,
                                            modifier = Modifier.padding(end = 14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Merge", fontWeight = FontWeight.Black,
                                                style = MaterialTheme.typography.titleMedium)
                                            Text("Combine matching symbols into more powerful ones. 3 minute session.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            Text("High Score: ${gamificationState.mergeHighScore}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(top = 4.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            viewModel.startMergeGame(gamificationState.currentTheme)
                                        }) {
                                            Text("Play")
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            item {
                                Text("Themes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                            }
                            val themes = listOf(
                                Triple("Cosmic Slate", 0, "🌌"),
                                Triple("Forest Sanctuary", 100, "🌿"),
                                Triple("Cyber Oasis", 250, "⚡")
                            )
                            items(themes) { (name, cost, emoji) ->
                                ThemeShopCard(
                                    themeName = name,
                                    emoji = emoji,
                                    cost = cost,
                                    isUnlocked = gamificationState.unlockedThemes.contains(name),
                                    isActive = gamificationState.currentTheme == name,
                                    onClick = { viewModel.buyTheme(name, cost) }
                                )
                            }
                        }
                        3 -> {
                            item {
                                Text(
                                    "Titles appear next to your level in the top bar. " +
                                    "Unlock them to show your progress.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            items(AvailableTitles) { titleReward ->
                                TitleCard(
                                    titleReward = titleReward,
                                    isUnlocked = gamificationState.unlockedTitleIds.contains(titleReward.id),
                                    isEquipped = gamificationState.equippedTitleId == titleReward.id,
                                    canAfford = gamificationState.dopamineGold >= titleReward.cost,
                                    onClick = { viewModel.buyTitle(titleReward.id, titleReward.cost) }
                                )
                            }
                        }
                    }
                }
            }
        }

        CustomSnackbarOverlay(message = snackbarMessage)
    }
}

@Composable
fun BreakActivityCard(
    activity: BreakActivity,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(activity.emoji, fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(activity.name, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⏱ ${activity.duration}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
                Text(activity.description, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp))
                Text("💎 ${activity.cost} DG", fontSize = 12.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                enabled = canAfford
            ) { Text("Take") }
        }
    }
}

@Composable
fun ThemeShopCard(
    themeName: String,
    emoji: String,
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(themeName, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text(
                when {
                    isActive -> "Active"
                    isUnlocked -> "Unlocked"
                    cost == 0 -> "Free"
                    else -> "💎 $cost DG"
                },
                color = when {
                    isActive -> MaterialTheme.colorScheme.primary
                    isUnlocked -> Color.Gray
                    else -> Color(0xFFFFD700)
                },
                fontSize = 13.sp
            )
        }
        if (!isActive) {
            Button(onClick = onClick) {
                Text(if (isUnlocked) "Apply" else "Buy")
            }
        }
    }
}

@Composable
fun TitleCard(
    titleReward: com.example.data.TitleReward,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titleReward.title, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isEquipped) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface)
                Text(titleReward.subtitle, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (!isUnlocked && titleReward.cost > 0) {
                    Text("💎 ${titleReward.cost} DG", fontSize = 12.sp,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
            when {
                isEquipped -> Text("✓ Equipped", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
                isUnlocked -> OutlinedButton(onClick = onClick) { Text("Equip") }
                titleReward.cost == 0 -> Button(onClick = onClick) { Text("Equip") }
                else -> Button(onClick = onClick, enabled = canAfford) { Text("Buy") }
            }
        }
    }
}

@Composable
fun BreakActivityDialog(
    activity: BreakActivity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(activity.emoji, fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(activity.name, fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge)
                        Text(activity.duration, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                activity.instructions.forEachIndexed { i, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("${i + 1}.", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp))
                        Text(step, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done — Back to Work")
                }
            }
        }
    }
}
