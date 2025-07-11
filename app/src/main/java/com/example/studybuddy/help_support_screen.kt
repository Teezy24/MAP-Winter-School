package com.studybuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel

data class FAQ(
    val question: String,
    val answer: String,
    val category: String
)

data class Tutorial(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val steps: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    viewModel: HelpSupportViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("FAQs", "Tutorials", "Contact", "Feedback")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Help & Support", 
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> FAQsContent(viewModel)
            1 -> TutorialsContent(viewModel)
            2 -> ContactContent(viewModel)
            3 -> FeedbackContent(viewModel)
        }
    }
}

@Composable
fun FAQsContent(viewModel: HelpSupportViewModel) {
    val faqs by viewModel.faqs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val filteredFAQs = remember(faqs, searchQuery, selectedCategory) {
        faqs.filter { faq ->
            val matchesSearch = searchQuery.isEmpty() || 
                faq.question.contains(searchQuery, ignoreCase = true) ||
                faq.answer.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = selectedCategory == "All" || faq.category == selectedCategory
            
            matchesSearch && matchesCategory
        }
    }
    
    val categories = remember(faqs) {
        listOf("All") + faqs.map { it.category }.distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search FAQs...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            selected = selectedCategory == category
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FAQ List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredFAQs) { faq ->
                FAQCard(faq = faq)
            }
        }
    }
}

@Composable
fun FAQCard(faq: FAQ) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(faq.category, fontSize = 12.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

@Composable
fun TutorialsContent(viewModel: HelpSupportViewModel) {
    val tutorials by viewModel.tutorials.collectAsState()
    
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tutorials) { tutorial ->
            TutorialCard(tutorial = tutorial)
        }
    }
}

@Composable
fun TutorialCard(tutorial: Tutorial) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = tutorial.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tutorial.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = tutorial.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Steps:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                tutorial.steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = step,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactContent(viewModel: HelpSupportViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Contact Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Contact Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                ContactItem(
                    icon = Icons.Default.Email,
                    title = "Email Support",
                    description = "support@studybuddy.com",
                    subtitle = "Response within 24 hours"
                )
                
                ContactItem(
                    icon = Icons.Default.Phone,
                    title = "Phone Support",
                    description = "+1 (555) 123-4567",
                    subtitle = "Mon-Fri 9AM-6PM EST"
                )
                
                ContactItem(
                    icon = Icons.Default.Chat,
                    title = "Live Chat",
                    description = "Available in-app",
                    subtitle = "Mon-Fri 9AM-9PM EST"
                )
            }
        }
        
        // Quick Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        icon = Icons.Default.Email,
                        text = "Send Email",
                        onClick = { /* Handle email */ }
                    )
                    
                    ActionButton(
                        icon = Icons.Default.Chat,
                        text = "Start Chat",
                        onClick = { /* Handle chat */ }
                    )
                    
                    ActionButton(
                        icon = Icons.Default.Phone,
                        text = "Call Us",
                        onClick = { /* Handle phone */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    title: String,
    description: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeedbackContent(viewModel: HelpSupportViewModel) {
    var feedbackType by remember { mutableStateOf("Bug Report") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    
    val feedbackTypes = listOf("Bug Report", "Feature Request", "General Feedback", "Complaint")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feedback Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Send Us Your Feedback",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Feedback Type Dropdown
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = feedbackType,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Feedback Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        feedbackTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    feedbackType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Rating (for general feedback)
                if (feedbackType == "General Feedback") {
                    Column {
                        Text(
                            text = "Rate your experience",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { rating = star }
                                ) {
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $star",
                                        tint = if (star <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Email Field
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Your Email (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Subject Field
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Message Field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    placeholder = { Text("Tell us about your experience or suggestion...") }
                )
                
                // Submit Button
                Button(
                    onClick = {
                        viewModel.submitFeedback(
                            type = feedbackType,
                            subject = subject,
                            message = message,
                            email = userEmail,
                            rating = rating
                        )
                        // Clear form
                        subject = ""
                        message = ""
                        userEmail = ""
                        rating = 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = subject.isNotBlank() && message.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Feedback")
                }
            }
        }
        
        // Feature Request Ideas
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Popular Feature Requests",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Help us prioritize what to build next!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                val popularFeatures = listOf(
                    "Study group collaboration",
                    "AI-powered study recommendations",
                    "Offline mode support",
                    "Integration with calendar apps",
                    "Advanced analytics dashboard"
                )
                
                popularFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                subject = "Feature Request: $feature"
                                feedbackType = "Feature Request"
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = feature,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ViewModel for Help & Support
class HelpSupportViewModel : androidx.lifecycle.ViewModel() {
    private val _faqs = mutableStateOf(
        listOf(
            FAQ(
                question = "How do I create a study schedule?",
                answer = "Go to the Schedule tab and tap the '+' button. Fill in the subject, date, time, and duration. Your schedule will appear in the calendar view.",
                category = "Scheduling"
            ),
            FAQ(
                question = "Can I set study goals?",
                answer = "Yes! Visit the Progress tab to set daily, weekly, or monthly study goals. You can track your progress and see productivity statistics.",
                category = "Goals"
            ),
            FAQ(
                question = "How do I add notes to my study materials?",
                answer = "In the Notes tab, tap the '+' button to create a new note. You can add text, links, attachments, and organize by subjects and tags.",
                category = "Notes"
            ),
            FAQ(
                question = "Can I sync my data across devices?",
                answer = "Yes, your data is automatically synced when you're signed in to your account. Make sure you're connected to the internet.",
                category = "Sync"
            ),
            FAQ(
                question = "How do I change my password?",
                answer = "Go to Settings > Account > Change Password. You'll need to enter your current password and then your new password twice.",
                category = "Account"
            ),
            FAQ(
                question = "Is there a dark mode?",
                answer = "Yes! You can enable dark mode in Settings > Appearance > Theme. Choose from Light, Dark, or System default.",
                category = "Appearance"
            ),
            FAQ(
                question = "How do I delete my account?",
                answer = "Contact our support team at support@studybuddy.com to request account deletion. We'll process your request within 7 business days.",
                category = "Account"
            ),
            FAQ(
                question = "Can I export my study data?",
                answer = "Yes, you can export your notes and schedules in PDF format from the Settings > Data Export section.",
                category = "Data"
            )
        )
    )
    
    private val _tutorials = mutableStateOf(
        listOf(
            Tutorial(
                title = "Getting Started",
                description = "Learn the basics of using StudyBuddy",
                icon = Icons.Default.PlayArrow,
                steps = listOf(
                    "Create your account and complete the setup",
                    "Set your first study goal in the Progress tab",
                    "Add your first study session in the Schedule tab",
                    "Create your first note in the Notes tab",
                    "Customize your preferences in Settings"
                )
            ),
            Tutorial(
                title = "Creating Effective Study Schedules",
                description = "Master the art of time management",
                icon = Icons.Default.Schedule,
                steps = listOf(
                    "Open the Schedule tab",
                    "Tap the '+' button to add a new session",
                    "Choose your subject and topic",
                    "Set a realistic time duration",
                    "Add breaks between long sessions",
                    "Review and adjust your schedule regularly"
                )
            ),
            Tutorial(
                title = "Organizing Your Notes",
                description = "Keep your study materials neat and accessible",
                icon = Icons.Default.Note,
                steps = listOf(
                    "Go to the Notes tab",
                    "Create notes for each subject",
                    "Use tags to categorize topics",
                    "Add links to online resources",
                    "Attach images or documents",
                    "Use the search function to find notes quickly"
                )
            ),
            Tutorial(
                title = "Tracking Your Progress",
                description = "Monitor your study habits and achievements",
                icon = Icons.Default.TrendingUp,
                steps = listOf(
                    "Visit the Progress tab",
                    "Set specific, measurable goals",
                    "Check in daily to mark completed sessions",
                    "Review your weekly productivity stats",
                    "Adjust goals based on your progress",
                    "Celebrate your achievements!"
                )
            )
        )
    )
    
    val faqs = _faqs
    val tutorials = _tutorials
    
    fun submitFeedback(
        type: String,
        subject: String,
        message: String,
        email: String,
        rating: Int
    ) {
        // Handle feedback submission
        // This would typically send data to your backend
        println("Feedback submitted: $type - $subject")
    }
}