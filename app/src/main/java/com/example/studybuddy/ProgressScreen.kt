package com.example.studybuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val snackbarHostState = remember { SnackbarHostState() }
    var newGoalText by remember { mutableStateOf("") }
    var goals by remember {
        mutableStateOf(
            listOf(
                Goal("Study 10 hours this week", 0.6f, false),
                Goal("Complete 3 assignments", 0.3f, false)
            )
        )
    }
    // Snackbar message state
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Show snackbar when message changes
    snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            // Add Goal Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = newGoalText,
                    onValueChange = { newGoalText = it },
                    label = { Text("New Goal") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newGoalText.isNotBlank()) {
                            goals = goals + Goal(newGoalText, 0f, false)
                            newGoalText = ""
                            snackbarMessage = "Goal added!"
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            // Dynamic Goals List
            if (goals.isEmpty()) {
                Text("No goals yet. Add one!", color = Color.Gray)
            } else {
                goals.forEachIndexed { idx, goal ->
                    GoalProgressItem(
                        goal = goal,
                        onCheckedChange = { checked ->
                            goals = goals.toMutableList().also {
                                it[idx] = it[idx].copy(completed = checked)
                            }
                            snackbarMessage = if (checked) "Goal completed!" else "Goal marked incomplete."
                        },
                        onRemove = {
                            goals = goals.toMutableList().also { it.removeAt(idx) }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

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
                // TODO: Replace with real pie chart
                Text("[Pie Chart Placeholder]", color = Color.DarkGray)
            }
        }
    }
}

data class Goal(
    val text: String,
    val progress: Float,
    val completed: Boolean
)

@Composable
fun GoalProgressItem(
    goal: Goal,
    onCheckedChange: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = goal.completed,
                onCheckedChange = onCheckedChange
            )
            Text(
                text = goal.text,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Remove goal"
                )
            }
        }
        LinearProgressIndicator(
            progress = if (goal.completed) 1f else goal.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 4.dp)
        )
    }
}