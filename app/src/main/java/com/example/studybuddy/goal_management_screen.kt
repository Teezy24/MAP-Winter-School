package com.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val type: GoalType,
    val targetValue: Int,
    val currentValue: Int = 0,
    val unit: String,
    val targetDate: Date,
    val isCompleted: Boolean = false,
    val createdDate: Date = Date(),
    val streak: Int = 0
)

enum class GoalType {
    SHORT_TERM, LONG_TERM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalManagementScreen(
    onNavigateBack: () -> Unit
) {
    var goals by remember { mutableStateOf(getSampleGoals()) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<Goal?>(null) }
    var showGoalDetails by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Goals") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showAddGoalDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        )

        // Goals Statistics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GoalStatItem(
                    title = "Total Goals",
                    value = goals.size.toString(),
                    icon = Icons.Default.Flag
                )
                GoalStatItem(
                    title = "Completed",
                    value = goals.count { it.isCompleted }.toString(),
                    icon = Icons.Default.CheckCircle
                )
                GoalStatItem(
                    title = "In Progress",
                    value = goals.count { !it.isCompleted }.toString(),
                    icon = Icons.Default.TrendingUp
                )
            }
        }

        // Filter Tabs
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("All", "Short-term", "Long-term", "Completed")
        
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Goals List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filteredGoals = when (selectedTab) {
                0 -> goals
                1 -> goals.filter { it.type == GoalType.SHORT_TERM }
                2 -> goals.filter { it.type == GoalType.LONG_TERM }
                3 -> goals.filter { it.isCompleted }
                else -> goals
            }

            items(filteredGoals) { goal ->
                GoalCard(
                    goal = goal,
                    onGoalClick = {
                        selectedGoal = goal
                        showGoalDetails = true
                    },
                    onProgressUpdate = { newValue ->
                        goals = goals.map { 
                            if (it.id == goal.id) {
                                it.copy(
                                    currentValue = newValue,
                                    isCompleted = newValue >= it.targetValue
                                )
                            } else it
                        }
                    }
                )
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onGoalAdded = { newGoal ->
                goals = goals + newGoal
                showAddGoalDialog = false
            }
        )
    }

    // Goal Details Dialog
    if (showGoalDetails && selectedGoal != null) {
        GoalDetailsDialog(
            goal = selectedGoal!!,
            onDismiss = { showGoalDetails = false },
            onGoalUpdated = { updatedGoal ->
                goals = goals.map { if (it.id == updatedGoal.id) updatedGoal else it }
            },
            onGoalDeleted = { goalId ->
                goals = goals.filter { it.id != goalId }
                showGoalDetails = false
            }
        )
    }
}

@Composable
fun GoalStatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onGoalClick: () -> Unit,
    onProgressUpdate: (Int) -> Unit
) {
    Card(
        onClick = onGoalClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Goal Type Badge
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = if (goal.type == GoalType.SHORT_TERM) "Short-term" else "Long-term",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (goal.type == GoalType.SHORT_TERM) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress: ${goal.currentValue}/${goal.targetValue} ${goal.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (goal.streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFire,
                            contentDescription = null,
                            tint = Color.Orange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${goal.streak} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Orange
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (goal.currentValue.toFloat() / goal.targetValue.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (goal.isCompleted) Color.Green else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Target Date
            Text(
                text = "Target: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(goal.targetDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Quick Actions
            if (!goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onProgressUpdate(goal.currentValue + 1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Progress")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onGoalAdded: (Goal) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf(GoalType.SHORT_TERM) }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time) }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add New Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Goal Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { goalType = GoalType.SHORT_TERM },
                        label = { Text("Short-term") },
                        selected = goalType == GoalType.SHORT_TERM,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { goalType = GoalType.LONG_TERM },
                        label = { Text("Long-term") },
                        selected = goalType == GoalType.LONG_TERM,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it },
                        label = { Text("Target") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Target Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(targetDate)}")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank() && targetValue.isNotBlank() && unit.isNotBlank()) {
                                val newGoal = Goal(
                                    id = UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    type = goalType,
                                    targetValue = targetValue.toIntOrNull() ?: 1,
                                    unit = unit,
                                    targetDate = targetDate
                                )
                                onGoalAdded(newGoal)
                            }
                        },
                        enabled = title.isNotBlank() && targetValue.isNotBlank() && unit.isNotBlank()
                    ) {
                        Text("Add Goal")
                    }
                }
            }
        }
    }
}

@Composable
fun GoalDetailsDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onGoalUpdated: (Goal) -> Unit,
    onGoalDeleted: (String) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Details
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Progress Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Current: ${goal.currentValue} ${goal.unit}")
                        Text("Target: ${goal.targetValue} ${goal.unit}")
                        Text("Completion: ${((goal.currentValue.toFloat() / goal.targetValue.toFloat()) * 100).toInt()}%")
                        
                        if (goal.streak > 0) {
                            Text("Streak: ${goal.streak} days")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!goal.isCompleted) {
                        Button(
                            onClick = {
                                onGoalUpdated(goal.copy(isCompleted = true, currentValue = goal.targetValue))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Complete")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onGoalDeleted(goal.id)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun getSampleGoals(): List<Goal> {
    val calendar = Calendar.getInstance()
    return listOf(
        Goal(
            id = "1",
            title = "Complete Math Assignment",
            description = "Finish chapters 1-3 exercises",
            type = GoalType.SHORT_TERM,
            targetValue = 3,
            currentValue = 2,
            unit = "chapters",
            targetDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 5) }.time,
            streak = 3
        ),
        Goal(
            id = "2",
            title = "Improve GPA",
            description = "Achieve a GPA of 3.5 or higher",
            type = GoalType.LONG_TERM,
            targetValue = 35,
            currentValue = 32,
            unit = "points",
            targetDate = calendar.apply { add(Calendar.MONTH, 6) }.time,
            streak = 15
        ),
        Goal(
            id = "3",
            title = "Read Scientific Papers",
            description = "Read 10 research papers this month",
            type = GoalType.SHORT_TERM,
            targetValue = 10,
            currentValue = 10,
            unit = "papers",
            targetDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -2) }.time,
            isCompleted = true
        )
    )
}