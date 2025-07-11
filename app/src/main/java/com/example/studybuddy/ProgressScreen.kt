package com.example.studybuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressScreen(
    onNavigate: (String) -> Unit = {},
    selected: String = "progress"
) {
    Scaffold(
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Goals & Progress Tracker",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = { /* TODO: Add goal logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Add Study Goal")
            }

            GoalProgressItem("Study 10 hours this week", 0.6f)
            Spacer(modifier = Modifier.height(12.dp))
            GoalProgressItem("Complete 3 assignments", 0.3f)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Weekly Productivity",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("[Pie Chart Placeholder]", color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun GoalProgressItem(goalText: String, progress: Float) {
    var checked by remember { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = { checked = it }
                )
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
            Text(
                text = goalText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
fun BottomNavigationBar(selected: String, onNavigate: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selected == "progress",
            onClick = { onNavigate("progress") },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Progress") },
            label = { Text("Progress") }
        )
        NavigationBarItem(
            selected = selected == "schedule",
            onClick = { onNavigate("schedule") },
            icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule") },
            label = { Text("Schedule") }
        )
        NavigationBarItem(
            selected = selected == "settings",
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
