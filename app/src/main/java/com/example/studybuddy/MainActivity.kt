package com.example.studybuddy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.studybuddy.ui.theme.DarkChart1
import com.example.studybuddy.ui.theme.DarkChart2
import com.example.studybuddy.ui.theme.LightChart1
import com.example.studybuddy.ui.theme.LightChart2
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase initialization (Firestore only)
        try {
            FirebaseApp.initializeApp(this)
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()

            Log.d("FirebaseTest", "Firebase App Initialized. Default App Name: ${FirebaseApp.getInstance().name}")
            Log.d("FirebaseTest", "FirebaseAuth instance: $auth")
            Log.d("FirebaseTest", "FirebaseFirestore instance: $firestore")
        } catch (e: Exception) {
            Log.e("FirebaseTest", "Error initializing Firebase: ${e.message}", e)
        }

        setContent {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            var isDarkMode by remember { mutableStateOf(false) }
            val db = remember { FirebaseFirestore.getInstance() }
            val context = this // Use activity context for notification (required for permission dialog)

            // Fetch theme from Firestore
            LaunchedEffect(userId) {
                userId?.let {
                    db.collection("users").document(it).get()
                        .addOnSuccessListener { doc ->
                            val theme = doc.getString("theme")
                            isDarkMode = theme == "dark"
                        }
                }
            }

            // Save theme to Firestore when changed
            fun saveThemeToFirestore(isDark: Boolean) {
                userId?.let {
                    db.collection("users").document(it)
                        .update("theme", if (isDark) "dark" else "light")
                }
            }

            StudyBuddyTheme(isDarkMode = isDarkMode) {
                var showLogin by rememberSaveable { mutableStateOf(true) }
                var showSignUp by rememberSaveable { mutableStateOf(false) }
                var isLoggedIn by rememberSaveable {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }
                if (!isLoggedIn) {
                    if (showSignUp) {
                        SignUpScreen(
                            onSignUpSuccess = {
                                isLoggedIn = true
                                showSignUp = false
                                // Request notification permission if needed, then show notification
                                requestAndShowSignUpNotification(context)
                            },
                            onNavigateToLogin = {
                                showSignUp = false
                                showLogin = true
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                                showLogin = false
                            },
                            onNavigateToSignUp = {
                                showSignUp = true
                                showLogin = false
                            },
                            onForgotPassword = {
                                // TODO: Implement password reset
                            }
                        )
                    }
                } else {
                    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                    val tabs = listOf("Home", "Progress", "Schedule", "Settings")
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Progress") },
                                    label = { Text("Progress") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Schedule") },
                                    label = { Text("Schedule") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            when (selectedTab) {
                                0 -> HomeScreen()
                                1 -> ProgressScreen(chartColors = if (isDarkMode) listOf(DarkChart1, DarkChart2) else listOf(LightChart1, LightChart2))
                                2 -> ScheduleScreen()
                                3 -> SettingsScreen(
                                    isDarkMode = isDarkMode,
                                    onDarkModeChange = {
                                        isDarkMode = it
                                        saveThemeToFirestore(it)
                                    },
                                    onNavigate = { dest ->
                                        if (dest == "logout") {
                                            FirebaseAuth.getInstance().signOut()
                                            isLoggedIn = false
                                            showLogin = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged: orientation=${newConfig.orientation}")
        // Optionally, handle UI or state changes here if needed
    }

    // Request notification permission if needed, then show notification
    private fun requestAndShowSignUpNotification(context: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf<String>(permission), 1002)
                // Notification will be shown after user grants permission (see onRequestPermissionsResult)
                return
            }
        }
        showSignUpNotification(context)
    }

    // Handle permission result for notifications
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showSignUpNotification(this)
        }
    }
    private fun showSignUpNotification(context: android.content.Context) {
        val channelId = "studybuddy_signup"
        val channelName = "StudyBuddy Signup"
        val notificationId = 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        try {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Welcome to StudyBuddy!")
                .setContentText("Thank you for signing up to StudyBuddy.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("Notification", "Notification permission denied: ${e.message}")
        }
    }
}

@Composable
fun BottomNavigationBar(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                label = { Text(tab) },
                icon = {
                    when (index) {
                        0 -> Icon(Icons.Default.Home, contentDescription = null)
                        1 -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        2 -> Icon(Icons.Default.DateRange, contentDescription = null)
                        3 -> Icon(Icons.Default.Settings, contentDescription = null)
                        else -> Icon(Icons.Default.Home, contentDescription = null)
                    }
                }
            )
        }
    }
}
