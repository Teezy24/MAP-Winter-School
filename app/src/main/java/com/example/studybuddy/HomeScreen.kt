package com.example.studybuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


data class Session(
    val id: String,
    val subject: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: String
)

@Composable
fun HomeScreen(
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var username by remember { mutableStateOf<String?>(null) }
    var usernameLoading by remember { mutableStateOf(true) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    val db = remember { FirebaseFirestore.getInstance() }

    // Upcoming events state
    var upcomingEvents by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var eventsLoading by remember { mutableStateOf(true) }
    var eventsError by remember { mutableStateOf<String?>(null) }

    // Current Goals (only incomplete)
    var goals by remember { mutableStateOf(listOf<Goal>()) }
    var goalsLoading by remember { mutableStateOf(true) }
    var goalsError by remember { mutableStateOf<String?>(null) }

    // Scheduled sessions state
    var sessions by remember { mutableStateOf(listOf<Session>()) }
    var sessionsLoading by remember { mutableStateOf(true) }
    var sessionsError by remember { mutableStateOf<String?>(null) }

    // Profile state
    var showProfile by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf<String?>(null) }
    var userInfoLoading by remember { mutableStateOf(false) }
    var userInfoError by remember { mutableStateOf<String?>(null) }
    var editableUsername by remember { mutableStateOf<String?>(null) }
    var saveInProgress by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }

    // Fetch username
    LaunchedEffect(userId) {
        usernameLoading = true
        usernameError = null
        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username")
                    usernameLoading = false
                }
                .addOnFailureListener { e ->
                    usernameError = "Failed to load username"
                    usernameLoading = false
                }
        }
    }

    // Fetch user's incomplete goals (live) - use DisposableEffect for proper cleanup
    DisposableEffect(userId) {
        goalsLoading = true
        goalsError = null
        val listener = userId?.let {
            db.collection("users").document(it).collection("goals")
                .whereEqualTo("completed", false)
                .addSnapshotListener { docs, e ->
                    if (e != null) {
                        goalsError = e.localizedMessage
                        goalsLoading = false
                        return@addSnapshotListener
                    }
                    if (docs != null) {
                        goals = docs.map { doc ->
                            Goal(
                                id = doc.id,
                                text = doc.getString("text") ?: "",
                                progress = doc.getDouble("progress")?.toFloat() ?: 0f,
                                completed = doc.getBoolean("completed") ?: false
                            )
                        }
                        goalsLoading = false
                    }
                }
        }
        onDispose {
            listener?.remove()
        }
    }


    // Fetch user's next scheduled sessions (show up to 3 upcoming) - use DisposableEffect for live updates
    DisposableEffect(userId) {
        sessionsLoading = true
        sessionsError = null
        val listener = userId?.let {
            db.collection("users").document(it).collection("sessions")
                .addSnapshotListener { docs, e ->
                    if (e != null) {
                        sessionsError = e.localizedMessage
                        sessionsLoading = false
                        return@addSnapshotListener
                    }
                    if (docs != null) {
                        sessions = docs.map { doc ->
                            Session(
                                id = doc.id,
                                subject = doc.getString("subject") ?: "",
                                date = doc.getString("date") ?: "",
                                startTime = doc.getString("startTime") ?: "",
                                endTime = doc.getString("endTime") ?: "",
                                duration = doc.getString("duration") ?: ""
                            )
                        }.sortedBy { it.date }.take(3)
                        sessionsLoading = false
                    }
                }
        }
        onDispose {
            listener?.remove()
        }
    }

    // Update goal completion (mark as complete and remove from Home)
    fun updateGoalCompletion(goal: Goal, completed: Boolean) {
        if (userId != null && goal.id != null) {
            db.collection("users").document(userId).collection("goals").document(goal.id)
                .update("completed", completed)
        }
    }

    // Fetch username and email for profile popup
    fun fetchUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            userInfoLoading = true
            userInfoError = null
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username")
                    editableUsername = username
                    userEmail = doc.getString("email")
                    userInfoLoading = false
                }
                .addOnFailureListener { e ->
                    userInfoError = "Failed to load user info"
                    userInfoLoading = false
                }
        }
    }

    // Save edited username
    fun saveUsername() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val newUsername = editableUsername?.trim()
        if (userId != null && !newUsername.isNullOrBlank()) {
            saveInProgress = true
            saveError = null
            saveSuccess = false
            // With open rules, just update
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("username", newUsername)
                .addOnSuccessListener {
                    username = newUsername
                    saveSuccess = true
                    saveInProgress = false
                }
                .addOnFailureListener { e ->
                    saveError = "Failed to save username"
                    saveInProgress = false
                }
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                // Username greeting with loading/error and profile icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when {
                        usernameLoading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading...", fontSize = 20.sp)
                            }
                        }
                        usernameError != null -> {
                            Text(
                                text = usernameError ?: "",
                                color = Color.Red,
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        else -> {
                            Text(
                                text = "Hello, ${username ?: "Student"} 👋",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    // Profile icon
                    IconButton(
                        onClick = {
                            if (!showProfile) fetchUserInfo()
                            showProfile = !showProfile
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Your Goals", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
            item {
                when {
                    goalsLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading goals...", fontSize = 16.sp)
                        }
                    }
                    goalsError != null -> {
                        Text(goalsError ?: "", color = Color.Black, fontSize = 16.sp)
                    }
                    goals.isEmpty() -> {
                        Text("No goals yet. Add one in Progress!", fontSize = 16.sp, color = Color.Gray)
                    }
                    else -> {
                        Column {
                            goals.forEach { goal ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        // Circular Checkbox
                                        TriStateCheckbox(
                                            state = if (goal.completed) androidx.compose.ui.state.ToggleableState.On else androidx.compose.ui.state.ToggleableState.Off,
                                            onClick = { updateGoalCompletion(goal, !goal.completed) },
                                            modifier = Modifier
                                                .size(24.dp)
                                        )
                                        Text(
                                            goal.text,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Scheduled Sessions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
            item {
                when {
                    sessionsLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading sessions...", fontSize = 16.sp)
                        }
                    }
                    sessionsError != null -> {
                        Text(sessionsError ?: "", color = Color.Blue, fontSize = 16.sp)
                    }
                    sessions.isEmpty() -> {
                        Text("No scheduled sessions.", fontSize = 16.sp, color = Color.Gray)
                    }
                    else -> {
                        Column {
                            sessions.forEach { session ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(session.subject, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${session.date} ${session.startTime}", color = Color.Gray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Calendar", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                val currentMonth = remember { YearMonth.now() }
                val daysInMonth = remember(currentMonth) { currentMonth.lengthOfMonth() }
                val firstDayOfMonth = remember(currentMonth) { currentMonth.atDay(1).dayOfWeek }
                val daysList = remember(currentMonth) {
                    (1..daysInMonth).map { day -> currentMonth.atDay(day) }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = MaterialTheme.shapes.medium
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DayOfWeek.values().forEach { dayOfWeek ->
                                Text(
                                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        val totalGridCount = daysInMonth + firstDayOfMonth.value - 1
                        val rows = (totalGridCount / 7) + 1
                        var dayIndex = 0
                        for (row in 0 until rows) {
                            Row(Modifier.fillMaxWidth()) {
                                for (col in 0..6) {
                                    val cellIndex = row * 7 + col
                                    if (cellIndex < firstDayOfMonth.value - 1 || dayIndex >= daysList.size) {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {}
                                    } else {
                                        val date = daysList[dayIndex]
                                        val isSelected = date == selectedDate
                                        val isToday = date == today
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                        else -> Color.Transparent
                                                    },
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .clickable { selectedDate = date },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onBackground
                                                },
                                                fontSize = 16.sp
                                            )
                                        }
                                        dayIndex++
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Profile popup dialog
        if (showProfile) {
            Dialog(
                onDismissRequest = { showProfile = false }
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.75f)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(24.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { showProfile = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Close Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (userInfoLoading) {
                            CircularProgressIndicator()
                        } else if (userInfoError != null) {
                            Text(
                                text = userInfoError ?: "",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            OutlinedTextField(
                                value = editableUsername ?: "",
                                onValueChange = {
                                    editableUsername = it
                                    saveError = null
                                    saveSuccess = false
                                },
                                label = { Text("Username") },
                                enabled = !saveInProgress,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = userEmail ?: "",
                                onValueChange = {},
                                label = { Text("Email") },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (saveError != null) {
                                Text(
                                    text = saveError ?: "",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (saveSuccess) {
                                Text(
                                    text = "Username updated!",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Button(
                                onClick = { saveUsername() },
                                enabled = !saveInProgress && !editableUsername.isNullOrBlank() && editableUsername != username,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                if (saveInProgress) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Save")
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { showProfile = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
