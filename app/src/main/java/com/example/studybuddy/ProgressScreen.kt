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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun ProgressScreen(
    onNavigate: (String) -> Unit = {},
    selected: String = "progress"
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var newGoalText by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var goals by remember { mutableStateOf(listOf<Goal>()) }
    val db = remember { FirebaseFirestore.getInstance() }

    // Use DisposableEffect for live updates and proper cleanup
    DisposableEffect(userId) {
        loading = true
        error = null
        val listener = userId?.let { uid ->
            db.collection("users").document(uid).collection("goals")
                .orderBy("completed", Query.Direction.ASCENDING)
                .addSnapshotListener { docs, e ->
                    if (e != null) {
                        error = e.localizedMessage ?: e.message
                        loading = false
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
                        loading = false
                    }
                }
        }
        onDispose { listener?.remove() }
    }
    var refreshGoalsTrigger by remember { mutableStateOf(false) }

    // Add new goal
    fun addGoal() {
        if (newGoalText.isNotBlank() && userId != null) {
            val goal = hashMapOf(
                "text" to newGoalText,
                "progress" to 0f,
                "completed" to false
            )
            db.collection("users").document(userId).collection("goals")
                .add(goal)
                .addOnSuccessListener {
                    snackbarMessage = "Goal added!"
                    newGoalText = ""
                    // Refetch goals
                    refreshGoalsTrigger = !refreshGoalsTrigger

                }
                .addOnFailureListener { e -> snackbarMessage = e.localizedMessage }
        }
    }
    LaunchedEffect(refreshGoalsTrigger) {
        // Refetch goals here
    }

    // Update goal completion
    fun updateGoalCompletion(goal: Goal, completed: Boolean) {
        if (userId != null && goal.id != null) {
            db.collection("users").document(userId).collection("goals").document(goal.id)
                .update("completed", completed)
                .addOnSuccessListener { snackbarMessage = if (completed) "Goal completed!" else "Goal marked incomplete." }
                .addOnFailureListener { e -> snackbarMessage = e.localizedMessage }
        }
    }

    // Remove goal
    fun removeGoal(goal: Goal) {
        if (userId != null && goal.id != null) {
            db.collection("users").document(userId).collection("goals").document(goal.id)
                .delete()
                .addOnSuccessListener { snackbarMessage = "Goal removed!" }
                .addOnFailureListener { e -> snackbarMessage = e.localizedMessage }
        }
    }

    var showSnackbar by remember { mutableStateOf(false) }

    // Show snackbar message
    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
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
                Button(onClick = { addGoal() }) { Text("Add") }
            }
            if (loading) {
                Text("Loading goals...", color = Color.Gray)
            } else if (error != null) {
                Text(error ?: "Error", color = Color.Red)
            } else if (goals.isEmpty()) {
                Text("No goals yet. Add one!", color = Color.Gray)
            } else {
                val (incomplete, complete) = goals.partition { !it.completed }
                (incomplete + complete).forEach { goal ->
                    GoalProgressItem(
                        goal = goal,
                        onCheckedChange = { checked -> updateGoalCompletion(goal, checked) },
                        onRemove = { removeGoal(goal) }
                    )
                    Spacer(modifier = Modifier.height(20.dp)) // Increased spacing
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
                Text("[Pie Chart Placeholder]", color = Color.DarkGray)
            }
        }
    }
}

data class Goal(
    val id: String? = null,
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
            progress = { if (goal.completed) 1f else goal.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 4.dp, horizontal = 10.dp),
        )
    }
}
