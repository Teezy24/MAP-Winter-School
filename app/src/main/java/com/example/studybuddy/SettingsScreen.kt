package com.example.studybuddy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    selected: String = "settings",
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLoggedOutSnackbar by remember { mutableStateOf(false) }

    // Show snackbar when needed
    if (showLoggedOutSnackbar) {
        LaunchedEffect(showLoggedOutSnackbar) {
            snackbarHostState.showSnackbar("Logged out")
            showLoggedOutSnackbar = false
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
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it },
                    thumbContent = null
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Enable Notifications",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Enable Notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    thumbContent = null
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Log out",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Log out")
            }

            // Confirmation dialog for logout
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Log out") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                showLogoutDialog = false
                                showLoggedOutSnackbar = true // trigger snackbar
                                onNavigate("logout")
                            }
                        ) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}