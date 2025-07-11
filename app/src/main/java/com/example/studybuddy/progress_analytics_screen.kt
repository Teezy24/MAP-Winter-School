package com.studybuddy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class StudyData(
    val day: String,
    val hours: Float,
    val target: Float = 4f
)

data class GoalProgress(
    val title: String,
    val current: Int,
    val target: Int,
    val category: String
)

data class WeeklyStats(
    val totalHours: Float,
    val averageDaily: Float,
    val goalsCompleted: Int,
    val streak: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressAnalyticsScreen() {
    var selectedPeriod by remember { mutableStateOf("Week") }
    
    // Sample data
    val weeklyData = remember {
        listOf(
            StudyData("Mon", 3.5f),
            StudyData("Tue", 4.2f),
            StudyData("Wed", 2.8f),
            StudyData("Thu", 5.1f),
            StudyData("Fri", 3.9f),
            StudyData("Sat", 4.5f),
            StudyData("Sun", 2.3f)
        )
    }
    
    val goals = remember {
        listOf(
            GoalProgress("Mathematics", 8, 10, "Study Hours"),
            GoalProgress("Physics", 12, 15, "Practice Problems"),
            GoalProgress("Chemistry", 6, 8, "Lab Reports"),
            GoalProgress("Biology", 20, 25, "Chapter Reviews")
        )
    }
    
    val weeklyStats = remember {
        WeeklyStats(
            totalHours = weeklyData.sumOf { it.hours.toDouble() }.toFloat(),
            averageDaily = weeklyData.sumOf { it.hours.toDouble() }.toFloat() / weeklyData.size,
            goalsCompleted = 3,
            streak = 5
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header with period selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress & Analytics",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                FilterChip(
                    onClick = { /* Handle period selection */ },
                    label = { Text(selectedPeriod) },
                    selected = true,
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )
            }
        }
        
        item {
            // Weekly Stats Cards
            WeeklyStatsSection(weeklyStats)
        }
        
        item {
            // Study Hours Chart
            StudyHoursChart(weeklyData)
        }
        
        item {
            // Goals Progress
            GoalsProgressSection(goals)
        }
        
        item {
            // Performance Patterns
            PerformancePatternsSection()
        }
        
        item {
            // Encouraging Messages
            EncouragingMessagesSection(weeklyStats)
        }
    }
}

@Composable
fun WeeklyStatsSection(stats: WeeklyStats) {
    Text(
        text = "This Week's Overview",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                title = "Total Hours",
                value = "${stats.totalHours.toInt()}h",
                icon = Icons.Default.AccessTime,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = "Daily Average",
                value = "${String.format("%.1f", stats.averageDaily)}h",
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            StatCard(
                title = "Goals Completed",
                value = stats.goalsCompleted.toString(),
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        item {
            StatCard(
                title = "Study Streak",
                value = "${stats.streak} days",
                icon = Icons.Default.LocalFire,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StudyHoursChart(data: List<StudyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Study Hours This Week",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = canvasWidth / data.size * 0.7f
                val maxHours = data.maxOf { it.hours }.coerceAtLeast(5f)
                
                data.forEachIndexed { index, studyData ->
                    val barHeight = (studyData.hours / maxHours) * canvasHeight * 0.8f
                    val x = index * (canvasWidth / data.size) + (canvasWidth / data.size - barWidth) / 2
                    val y = canvasHeight * 0.9f - barHeight
                    
                    // Draw bar
                    drawRect(
                        color = if (studyData.hours >= studyData.target) 
                            Color(0xFF4CAF50) else Color(0xFFFF9800),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                    
                    // Draw day label
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            studyData.day,
                            x + barWidth / 2,
                            canvasHeight * 0.95f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 32f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                    
                    // Draw hours value
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "${studyData.hours}h",
                            x + barWidth / 2,
                            y - 20f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalsProgressSection(goals: List<GoalProgress>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Goals Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            goals.forEach { goal ->
                GoalProgressItem(goal)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun GoalProgressItem(goal: GoalProgress) {
    val progress = goal.current.toFloat() / goal.target.toFloat()
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = goal.title,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${goal.current}/${goal.target}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = goal.category,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = if (progress >= 0.8f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PerformancePatternsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance Patterns",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Peak Performance Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Peak Performance Time",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "2:00 PM - 4:00 PM",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Most Productive Subject
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Most Productive Subject",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Mathematics (85% completion rate)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Study Consistency
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Study Consistency",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "6 out of 7 days this week",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EncouragingMessagesSection(stats: WeeklyStats) {
    val message = when {
        stats.streak >= 7 -> "🔥 Amazing! You're on a 7-day streak! Keep the momentum going!"
        stats.averageDaily >= 4f -> "⭐ Great job! You're consistently meeting your daily goals!"
        stats.goalsCompleted >= 3 -> "🎯 Excellent progress! You're crushing your goals this week!"
        else -> "💪 You're doing great! Every study session counts towards your success!"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌟",
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}