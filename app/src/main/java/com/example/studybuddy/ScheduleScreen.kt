package com.example.studybuddy

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


@Composable
fun ScheduleScreen(
    onNavigate: (String) -> Unit = {},
    selected: String = "schedule"
) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = remember { FirebaseFirestore.getInstance() }
    // List of scheduled sessions
    var sessions by remember { mutableStateOf(listOf<Session>()) }
    var loading by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    // Validation error state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Snackbar message state
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Add a trigger to refetch sessions
    var refreshSessions by remember { mutableStateOf(false) }

    // Fetch sessions from Firestore (live, real-time updates)
    DisposableEffect(userId, refreshSessions) {
        if (userId == null) {
            loading = false
            fetchError = "User not logged in"
            return@DisposableEffect onDispose { }
        }
        loading = true
        fetchError = null
        val listener = db.collection("users").document(userId).collection("sessions")
            .addSnapshotListener { docs, e ->
                if (e != null) {
                    fetchError = e.localizedMessage
                    loading = false
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
                    }
                    loading = false
                }
            }
        onDispose {
            listener.remove()
        }
    }

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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Add Study Session", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerDialog(context,
                            { _, year, month, day ->
                                date = "$day/${month + 1}/$year"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = startTime,
                onValueChange = { },
                label = { Text("Start Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        TimePickerDialog(context,
                            { _, hour, minute ->
                                startTime = String.format("%02d:%02d", hour, minute)
                                if (endTime.isNotBlank()) {
                                    duration = calculateDuration(startTime, endTime)
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick Start Time")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = endTime,
                onValueChange = { },
                label = { Text("End Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        TimePickerDialog(context,
                            { _, hour, minute ->
                                endTime = String.format("%02d:%02d", hour, minute)
                                if (startTime.isNotBlank()) {
                                    duration = calculateDuration(startTime, endTime)
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick End Time")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Validation
                    if (subject.isBlank() || date.isBlank() || startTime.isBlank() || endTime.isBlank() || duration.isBlank()) {
                        errorMessage = "Please fill in all fields."
                    } else if (duration.toIntOrNull() == null || duration.toInt() <= 0) {
                        errorMessage = "Duration must be a positive number."
                    } else if (userId != null) {
                        // Add session to Firestore
                        val session = hashMapOf(
                            "subject" to subject,
                            "date" to date,
                            "startTime" to startTime,
                            "endTime" to endTime,
                            "duration" to duration
                        )
                        db.collection("users").document(userId).collection("sessions")
                            .add(session)
                            .addOnSuccessListener {
                                snackbarMessage = "Study session added!"
                                subject = ""
                                date = ""
                                startTime = ""
                                endTime = ""
                                duration = ""
                                errorMessage = null
                                // Trigger refetch (not strictly needed with snapshot listener, but kept for compatibility)
                                refreshSessions = !refreshSessions
                            }
                            .addOnFailureListener { e -> errorMessage = e.localizedMessage }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add")
            }
            // Show error message if any
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show scheduled sessions
            if (loading) {
                Text("Loading sessions...", color = MaterialTheme.colorScheme.primary)
            } else if (fetchError != null) {
                Text(fetchError ?: "Error", color = MaterialTheme.colorScheme.error)
            } else if (sessions.isNotEmpty()) {
                Text(
                    "Scheduled Sessions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    items(sessions) { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(session.subject, fontWeight = FontWeight.SemiBold)
                                Text("Date: ${session.date}")
                                Text("Time: ${session.startTime} - ${session.endTime}")
                                Text("Duration: ${session.duration} min")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to calculate duration in minutes from two "HH:mm" strings
private fun calculateDuration(start: String, end: String): String {
    return try {
        val (sh, sm) = start.split(":").map { it.toInt() }
        val (eh, em) = end.split(":").map { it.toInt() }
        val startMinutes = sh * 60 + sm
        val endMinutes = eh * 60 + em
        val diff = endMinutes - startMinutes
        if (diff > 0) diff.toString() else ""
    } catch (e: Exception) {
        ""
    }
}