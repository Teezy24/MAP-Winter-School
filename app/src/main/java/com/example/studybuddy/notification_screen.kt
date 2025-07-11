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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Reminder(
    val id: String,
    val title: String,
    val description: String,
    val dateTime: LocalDateTime,
    val type: ReminderType,
    val isActive: Boolean = true
)

enum class ReminderType {
    STUDY_SESSION, DEADLINE, ASSIGNMENT, EXAM, BREAK
}

data class NotificationSettings(
    val enableNotifications: Boolean = true,
    val studySessionReminders: Boolean = true,
    val deadlineReminders: Boolean = true,
    val reminderFrequency: Int = 15, // minutes before
    val notificationTone: String = "Default"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationReminderScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var settings by remember { mutableStateOf(NotificationSettings()) }
    
    // Sample reminders data
    val reminders = remember {
        listOf(
            Reminder(
                "1",
                "Math Study Session",
                "Algebra practice - Chapter 5",
                LocalDateTime.now().plusHours(2),
                ReminderType.STUDY_SESSION
            ),
            Reminder(
                "2",
                "Physics Assignment Due",
                "Submit lab report by midnight",
                LocalDateTime.now().plusDays(1),
                ReminderType.DEADLINE
            ),
            Reminder(
                "3",
                "Chemistry Exam",
                "Final exam - Organic Chemistry",
                LocalDateTime.now().plusDays(3),
                ReminderType.EXAM
            ),
            Reminder(
                "4",
                "Study Break",
                "Take a 15-minute break",
                LocalDateTime.now().plusMinutes(45),
                ReminderType.BREAK
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Notifications & Reminders",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active Reminders") },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTab) {
            0 -> ActiveRemindersContent(reminders)
            1 -> NotificationSettingsContent(settings) { settings = it }
        }
    }
}

@Composable
fun ActiveRemindersContent(reminders: List<Reminder>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (reminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active reminders",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(reminders) { reminder ->
                ReminderCard(reminder)
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (reminder.type) {
                ReminderType.STUDY_SESSION -> MaterialTheme.colorScheme.primaryContainer
                ReminderType.DEADLINE -> MaterialTheme.colorScheme.errorContainer
                ReminderType.EXAM -> MaterialTheme.colorScheme.tertiaryContainer
                ReminderType.BREAK -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (reminder.type) {
                    ReminderType.STUDY_SESSION -> Icons.Default.Book
                    ReminderType.DEADLINE -> Icons.Default.Warning
                    ReminderType.EXAM -> Icons.Default.School
                    ReminderType.BREAK -> Icons.Default.Coffee
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when (reminder.type) {
                    ReminderType.STUDY_SESSION -> MaterialTheme.colorScheme.onPrimaryContainer
                    ReminderType.DEADLINE -> MaterialTheme.colorScheme.onErrorContainer
                    ReminderType.EXAM -> MaterialTheme.colorScheme.onTertiaryContainer
                    ReminderType.BREAK -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = reminder.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = reminder.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = reminder.isActive,
                onCheckedChange = { /* Handle toggle */ }
            )
        }
    }
}

@Composable
fun NotificationSettingsContent(
    settings: NotificationSettings,
    onSettingsChange: (NotificationSettings) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "General Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Enable Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Enable Notifications", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Receive all app notifications",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.enableNotifications,
                            onCheckedChange = { 
                                onSettingsChange(settings.copy(enableNotifications = it))
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Study Session Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Study Session Reminders", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Get notified before study sessions",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.studySessionReminders,
                            onCheckedChange = { 
                                onSettingsChange(settings.copy(studySessionReminders = it))
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Deadline Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Deadline Reminders", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Get notified about upcoming deadlines",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.deadlineReminders,
                            onCheckedChange = { 
                                onSettingsChange(settings.copy(deadlineReminders = it))
                            }
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reminder Frequency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Remind me ${settings.reminderFrequency} minutes before",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Slider(
                        value = settings.reminderFrequency.toFloat(),
                        onValueChange = { 
                            onSettingsChange(settings.copy(reminderFrequency = it.toInt()))
                        },
                        valueRange = 5f..60f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "5 min", fontSize = 12.sp)
                        Text(text = "60 min", fontSize = 12.sp)
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notification Tone",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val tones = listOf("Default", "Chime", "Bell", "Gentle", "Alert")
                    
                    tones.forEach { tone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.notificationTone == tone,
                                onClick = { 
                                    onSettingsChange(settings.copy(notificationTone = tone))
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = tone)
                        }
                    }
                }
            }
        }
    }
}