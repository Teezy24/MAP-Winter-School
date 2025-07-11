package com.example.studybuddy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var usernameOrEmailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    fun validateAndLogin() {
        usernameOrEmailError = usernameOrEmail.isBlank()
        passwordError = password.isBlank()
        if (usernameOrEmailError || passwordError) {
            errorMessage = "Please fill in all fields."
            showError = true
            coroutineScope.launch {
                delay(5000)
                showError = false
            }
            return
        }
        if (usernameOrEmail.contains("@")) {
            // Login with email
            auth.signInWithEmailAndPassword(usernameOrEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        errorMessage = task.exception?.localizedMessage ?: "Invalid credentials."
                        showError = true
                        usernameOrEmailError = true
                        passwordError = true
                        coroutineScope.launch {
                            delay(5000)
                            showError = false
                        }
                    }
                }
        } else {
            // Login with username: lookup email in Firestore
            db.collection("users").whereEqualTo("username", usernameOrEmail).get().addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val email = docs.documents[0].getString("email") ?: ""
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Invalid credentials."
                                showError = true
                                usernameOrEmailError = true
                                passwordError = true
                                coroutineScope.launch {
                                    delay(5000)
                                    showError = false
                                }
                            }
                        }
                } else {
                    errorMessage = "Username not found."
                    showError = true
                    usernameOrEmailError = true
                    coroutineScope.launch {
                        delay(5000)
                        showError = false
                    }
                }
            }.addOnFailureListener { e ->
                errorMessage = e.localizedMessage ?: "Login failed."
                showError = true
                coroutineScope.launch {
                    delay(5000)
                    showError = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = usernameOrEmail,
                onValueChange = {
                    usernameOrEmail = it
                    usernameOrEmailError = false
                },
                label = { Text("Username or Email") },
                isError = usernameOrEmailError,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                },
                label = { Text("Password") },
                isError = passwordError,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                Text("Forgot Password?", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { validateAndLogin() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(visible = showError, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(fontSize = 14.sp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ")
                Text(
                    text = "Sign up",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToSignUp)
                        .padding(2.dp)
                )
            }
        }
    }
}

