package com.example.studybuddy

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    selected: String = "settings",
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onNavigate("logout")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Log out")
            }
        }
    }
}
