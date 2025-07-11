package com.studybuddy.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    var selectedAcademicLevel by remember { mutableStateOf("") }
    var selectedStudyMethods by remember { mutableStateOf(setOf<String>()) }
    var selectedSubjects by remember { mutableStateOf(setOf<String>()) }
    var goalPreferences by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (pagerState.currentPage + 1) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomeScreen()
                1 -> PersonalizationScreen(
                    selectedAcademicLevel = selectedAcademicLevel,
                    onAcademicLevelSelected = { selectedAcademicLevel = it },
                    selectedStudyMethods = selectedStudyMethods,
                    onStudyMethodsChanged = { selectedStudyMethods = it },
                    selectedSubjects = selectedSubjects,
                    onSubjectsChanged = { selectedSubjects = it }
                )
                2 -> GoalPreferencesScreen(
                    goalPreferences = goalPreferences,
                    onGoalPreferencesChanged = { goalPreferences = it }
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onOnboardingComplete()
                    }
                },
                enabled = when (pagerState.currentPage) {
                    1 -> selectedAcademicLevel.isNotEmpty()
                    2 -> goalPreferences.isNotEmpty()
                    else -> true
                }
            ) {
                Text(
                    if (pagerState.currentPage < 2) "Next" else "Get Started"
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to StudyBuddy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your personal study companion to help you stay organized, motivated, and achieve your academic goals.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureItem(
                icon = Icons.Default.DateRange,
                title = "Smart Scheduling",
                description = "Plan your study sessions effectively"
            )
            
            FeatureItem(
                icon = Icons.Default.TrendingUp,
                title = "Progress Tracking",
                description = "Monitor your academic progress"
            )
            
            FeatureItem(
                icon = Icons.Default.Notifications,
                title = "Smart Reminders",
                description = "Never miss important deadlines"
            )
        }
    }
}

@Composable
fun PersonalizationScreen(
    selectedAcademicLevel: String,
    onAcademicLevelSelected: (String) -> Unit,
    selectedStudyMethods: Set<String>,
    onStudyMethodsChanged: (Set<String>) -> Unit,
    selectedSubjects: Set<String>,
    onSubjectsChanged: (Set<String>) -> Unit
) {
    val academicLevels = listOf("High School", "University", "Graduate School", "Other")
    val studyMethods = listOf("Visual Learning", "Reading", "Practice Problems", "Group Study", "Flashcards", "Note-taking")
    val subjects = listOf("Mathematics", "Science", "History", "Literature", "Languages", "Computer Science", "Art", "Music")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Let's personalize your experience",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Academic Level
        Text(
            text = "Academic Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        academicLevels.forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAcademicLevel == level,
                    onClick = { onAcademicLevelSelected(level) }
                )
                Text(
                    text = level,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Study Methods
        Text(
            text = "Preferred Study Methods",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        studyMethods.chunked(2).forEach { rowMethods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMethods.forEach { method ->
                    FilterChip(
                        onClick = {
                            onStudyMethodsChanged(
                                if (selectedStudyMethods.contains(method)) {
                                    selectedStudyMethods - method
                                } else {
                                    selectedStudyMethods + method
                                }
                            )
                        },
                        label = { Text(method) },
                        selected = selectedStudyMethods.contains(method),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Subjects
        Text(
            text = "Subjects of Interest",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        subjects.chunked(2).forEach { rowSubjects ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSubjects.forEach { subject ->
                    FilterChip(
                        onClick = {
                            onSubjectsChanged(
                                if (selectedSubjects.contains(subject)) {
                                    selectedSubjects - subject
                                } else {
                                    selectedSubjects + subject
                                }
                            )
                        },
                        label = { Text(subject) },
                        selected = selectedSubjects.contains(subject),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GoalPreferencesScreen(
    goalPreferences: Set<String>,
    onGoalPreferencesChanged: (Set<String>) -> Unit
) {
    val goals = listOf(
        "Improve GPA",
        "Better Time Management",
        "Exam Preparation",
        "Assignment Completion",
        "Study Consistency",
        "Reduce Procrastination",
        "Skill Development",
        "Knowledge Retention"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "What are your main goals?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Select areas where you'd like to improve (choose at least one)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        goals.forEach { goal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (goalPreferences.contains(goal)) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                onClick = {
                    onGoalPreferencesChanged(
                        if (goalPreferences.contains(goal)) {
                            goalPreferences - goal
                        } else {
                            goalPreferences + goal
                        }
                    )
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goal,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (goalPreferences.contains(goal)) FontWeight.Medium else FontWeight.Normal
                    )
                    
                    if (goalPreferences.contains(goal)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}