package com.studybuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class TaskType {
    ASSIGNMENT, EXAM, QUIZ, PROJECT
}

enum class TaskStatus {
    UPCOMING, IN_PROGRESS, COMPLETED, OVERDUE
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val type: TaskType,
    val subject: String,
    val dueDate: LocalDateTime,
    val createdDate: LocalDateTime,
    val priority: Priority,
    val status: TaskStatus,
    val reminderEnabled: Boolean = true,
    val reminderTime: LocalDateTime? = null,
    val estimatedHours: Int = 0,
    val actualHours: Int = 0,
    val progress: Float = 0f // 0.0 to 1.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAssignmentTrackerScreen() {
    var tasks by remember { mutableStateOf(getSampleTasks()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Due Date") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        TopAppBar(
            title = { 
                Text(
                    "Tasks & Assignments",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Filter menu
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        val filters = listOf("All", "Upcoming", "In Progress", "Completed", "Overdue")
                        filters.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Sort menu
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        val sortOptions = listOf("Due Date", "Priority", "Subject", "Progress")
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    sortBy = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Summary Cards
        TaskSummaryCards(tasks = tasks)

        // Filter chips
        FilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        // Tasks List
        val filteredTasks = filterTasks(tasks, selectedFilter)
        val sortedTasks = sortTasks(filteredTasks, sortBy)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedTasks) { task ->
                TaskCard(
                    task = task,
                    onTaskClick = { /* Handle task click */ },
                    onStatusChange = { updatedTask ->
                        tasks = tasks.map { if (it.id == updatedTask.id) updatedTask else it }
                    }
                )
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { newTask ->
                tasks = tasks + newTask
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskSummaryCards(tasks: List<Task>) {
    val upcomingCount = tasks.count { it.status == TaskStatus.UPCOMING }
    val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val overdueCount = tasks.count { it.status == TaskStatus.OVERDUE }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Upcoming",
                    count = upcomingCount,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "In Progress",
                    count = inProgressCount,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Completed",
                    count = completedCount,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Overdue",
                    count = overdueCount,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Upcoming", "In Progress", "Completed", "Overdue")
    
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onStatusChange: (Task) -> Unit
) {
    val priorityColor = when (task.priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.MEDIUM -> Color(0xFFFF9800)
        Priority.HIGH -> Color(0xFFFF5722)
        Priority.URGENT -> Color(0xFFF44336)
    }

    val statusColor = when (task.status) {
        TaskStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        TaskStatus.COMPLETED -> Color(0xFF4CAF50)
        TaskStatus.OVERDUE -> Color(0xFFF44336)
    }

    val daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), task.dueDate.toLocalDate())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.status == TaskStatus.COMPLETED) 
                            TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = task.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Priority indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Progress bar
            if (task.progress > 0f) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(task.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = task.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due date and type
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            when (task.type) {
                                TaskType.ASSIGNMENT -> Icons.Default.Assignment
                                TaskType.EXAM -> Icons.Default.Quiz
                                TaskType.QUIZ -> Icons.Default.HelpOutline
                                TaskType.PROJECT -> Icons.Default.Work
                            },
                            contentDescription = task.type.name,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.type.name.lowercase().capitalize(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Due Date",
                            modifier = Modifier.size(16.dp),
                            tint = if (daysUntilDue < 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when {
                                daysUntilDue < 0 -> "Overdue by ${-daysUntilDue} days"
                                daysUntilDue == 0L -> "Due today"
                                daysUntilDue == 1L -> "Due tomorrow"
                                else -> "Due in $daysUntilDue days"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysUntilDue < 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip and actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status chip
                    AssistChip(
                        onClick = { 
                            val newStatus = when (task.status) {
                                TaskStatus.UPCOMING -> TaskStatus.IN_PROGRESS
                                TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
                                TaskStatus.COMPLETED -> TaskStatus.UPCOMING
                                TaskStatus.OVERDUE -> TaskStatus.IN_PROGRESS
                            }
                            onStatusChange(task.copy(status = newStatus))
                        },
                        label = { 
                            Text(
                                text = task.status.name.replace("_", " "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                when (task.status) {
                                    TaskStatus.UPCOMING -> Icons.Default.Schedule
                                    TaskStatus.IN_PROGRESS -> Icons.Default.PlayArrow
                                    TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                                    TaskStatus.OVERDUE -> Icons.Default.Warning
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = statusColor.copy(alpha = 0.1f),
                            labelColor = statusColor,
                            leadingIconContentColor = statusColor
                        )
                    )

                    // Reminder icon
                    if (task.reminderEnabled) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Reminder Set",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.ASSIGNMENT) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var estimatedHours by remember { mutableStateOf("2") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Box {
                    OutlinedTextField(
                        value = selectedType.name.lowercase().capitalize(),
                        onValueChange = { },
                        label = { Text("Type") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showTypeMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Type")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        TaskType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().capitalize()) },
                                onClick = {
                                    selectedType = type
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }

                // Priority selector
                Box {
                    OutlinedTextField(
                        value = selectedPriority.name.lowercase().capitalize(),
                        onValueChange = { },
                        label = { Text("Priority") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showPriorityMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Priority")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showPriorityMenu,
                        onDismissRequest = { showPriorityMenu = false }
                    ) {
                        Priority.values().forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name.lowercase().capitalize()) },
                                onClick = {
                                    selectedPriority = priority
                                    showPriorityMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = { },
                    label = { Text("Due Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                OutlinedTextField(
                    value = estimatedHours,
                    onValueChange = { estimatedHours = it },
                    label = { Text("Estimated Hours") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                    Text("Enable reminders")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTask = Task(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        description = description,
                        type = selectedType,
                        subject = subject,
                        dueDate = dueDate.atTime(23, 59),
                        createdDate = LocalDateTime.now(),
                        priority = selectedPriority,
                        status = TaskStatus.UPCOMING,
                        reminderEnabled = reminderEnabled,
                        estimatedHours = estimatedHours.toIntOrNull() ?: 0
                    )
                    onTaskAdded(newTask)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun filterTasks(tasks: List<Task>, filter: String): List<Task> {
    return when (filter) {
        "Upcoming" -> tasks.filter { it.status == TaskStatus.UPCOMING }
        "In Progress" -> tasks.filter { it.status == TaskStatus.IN_PROGRESS }
        "Completed" -> tasks.filter { it.status == TaskStatus.COMPLETED }
        "Overdue" -> tasks.filter { it.status == TaskStatus.OVERDUE }
        else -> tasks
    }
}

private fun sortTasks(tasks: List<Task>, sortBy: String): List<Task> {
    return when (sortBy) {
        "Due Date" -> tasks.sortedBy { it.dueDate }
        "Priority" -> tasks.sortedByDescending { it.priority.ordinal }
        "Subject" -> tasks.sortedBy { it.subject }
        "Progress" -> tasks.sortedByDescending { it.progress }
        else -> tasks
    }
}

private fun getSampleTasks(): List<Task> {
    val now = LocalDateTime.now()
    return listOf(
        Task(
            id = "1",
            title = "Mathematics Assignment #3",
            description = "Complete calculus problems on integration and differentiation",
            type = TaskType.ASSIGNMENT,
            subject = "Mathematics",
            dueDate = now.plusDays(3),
            createdDate = now.minusDays(2),
            priority = Priority.HIGH,
            status = TaskStatus.IN_PROGRESS,
            estimatedHours = 4,
            actualHours = 2,
            progress = 0.6f
        ),
        Task(
            id = "2",
            title = "Physics Lab Report",
            description = "Write report on pendulum motion experiment",
            type = TaskType.ASSIGNMENT,
            subject = "Physics",
            dueDate = now.plusDays(1),
            createdDate = now.minusDays(5),
            priority = Priority.URGENT,
            status = TaskStatus.UPCOMING,
            estimatedHours = 3,
            progress = 0.2f
        ),
        Task(
            id = "3",
            title = "Chemistry Midterm",
            description = "Midterm exam covering organic chemistry chapters 1-5",
            type = TaskType.EXAM,
            subject = "Chemistry",
            dueDate = now.plusDays(7),
            createdDate = now.minusDays(10),
            priority = Priority.HIGH,
            status = TaskStatus.UPCOMING,
            estimatedHours = 15
        ),
        Task(
            id = "4",
            title = "Literature Essay",
            description = "Analysis of Shakespeare's Hamlet",
            type = TaskType.ASSIGNMENT,
            subject = "Literature",
            dueDate = now.minusDays(1),
            createdDate = now.minusDays(14),
            priority = Priority.MEDIUM,
            status = TaskStatus.OVERDUE,
            estimatedHours = 6,
            actualHours = 3,
            progress = 0.8f
        ),
        Task(
            id = "5",
            title = "Biology Quiz",
            description = "Weekly quiz on cell biology",
            type = TaskType.QUIZ,
            subject = "Biology",
            dueDate = now.plusDays(2),
            createdDate = now.minusDays(1),
            priority = Priority.MEDIUM,
            status = TaskStatus.UPCOMING,
            estimatedHours = 2
        ),
        Task(
            id = "6",
            title = "Computer Science Project",
            description = "Build a web application using React and Node.js",
            type = TaskType.PROJECT,
            subject = "Computer Science",
            dueDate = now.plusDays(14),
            createdDate = now.minusDays(7),
            priority = Priority.HIGH,
            status = TaskStatus.IN_PROGRESS,
            estimatedHours = 25,
            actualHours = 8,
            progress = 0.3f
        )
    )
}