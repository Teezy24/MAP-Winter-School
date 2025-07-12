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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    // Fetch upcoming events (for today and future) - now with real-time updates
    DisposableEffect(userId) {
        eventsLoading = true
        eventsError = null
        val listener = userId?.let {
            db.collection("events")
                .whereEqualTo("userId", it)
                .whereGreaterThanOrEqualTo("date", today.toString())
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(5)
                .addSnapshotListener { docs, e ->
                    if (e != null) {
                        eventsError = "Failed to load events"
                        eventsLoading = false
                        return@addSnapshotListener
                    }
                    if (docs != null) {
                        upcomingEvents = docs.map { doc ->
                            val title = doc.getString("title") ?: "Untitled"
                            val date = doc.getString("date") ?: ""
                            val time = doc.getString("time") ?: ""
                            val display = if (date == today.toString()) "Today at $time" else "$date at $time"
                            title to display
                        }
                        eventsLoading = false
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

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Username greeting with loading/error
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
                        fontSize = 20.sp
                    )
                }
                else -> {
                    Text(
                        text = "Hello, ${username ?: "Student"} 👋",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Show user's incomplete goals above events
            Text("Your Goals", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            when {
                goalsLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading goals...", fontSize = 16.sp)
                    }
                }
                goalsError != null -> {
                    Text(goalsError ?: "", color = Color.Red, fontSize = 16.sp)
                }
                goals.isEmpty() -> {
                    Text("No goals yet. Add one in Progress!", fontSize = 16.sp, color = Color.Gray)
                }
                else -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                        items(goals.size) { idx ->
                            val goal = goals[idx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp) // Increased spacing
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Checkbox(
                                        checked = goal.completed,
                                        onCheckedChange = { checked ->
                                            updateGoalCompletion(goal, checked)
                                        }
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

            // Show user's next scheduled sessions
            Text("Scheduled Sessions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            when {
                sessionsLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading sessions...", fontSize = 16.sp)
                    }
                }
                sessionsError != null -> {
                    Text(sessionsError ?: "", color = Color.Red, fontSize = 16.sp)
                }
                sessions.isEmpty() -> {
                    Text("No scheduled sessions.", fontSize = 16.sp, color = Color.Gray)
                }
                else -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 80.dp)) {
                        items(sessions.size) { idx ->
                            val session = sessions[idx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp) // Increased spacing
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

            Text("Coming up", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            when {
                eventsLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading events...", fontSize = 16.sp)
                    }
                }
                eventsError != null -> {
                    Text(
                        text = eventsError ?: "",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
                upcomingEvents.isEmpty() -> {
                    Text("No upcoming events.", fontSize = 16.sp, color = Color.Gray)
                }
                else -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                        items(upcomingEvents.size) { idx ->
                            val (title, display) = upcomingEvents[idx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp) // Increased spacing
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text(display, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Calendar", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

            val currentMonth = remember { YearMonth.now() }
            val daysInMonth = remember(currentMonth) { currentMonth.lengthOfMonth() }
            val firstDayOfMonth = remember(currentMonth) { currentMonth.atDay(1).dayOfWeek }
            val daysList = remember(currentMonth) {
                (1..daysInMonth).map { day -> currentMonth.atDay(day) }
            }

            // Calendar container with border and rounded corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        color = Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Column {
                    // Month header
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    // Days of week row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DayOfWeek.values().forEach { dayOfWeek ->
                            Text(
                                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Calendar days grid
                    val totalGridCount = daysInMonth + firstDayOfMonth.value - 1
                    val rows = (totalGridCount / 7) + 1
                    Column {
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
                                                        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
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
                                                    else -> Color.Black
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
            }
        }
    }
}
