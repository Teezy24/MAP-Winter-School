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
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var retypePasswordError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    fun isValidEmail(email: String): Boolean = email.contains("@") && email.contains(".")
    fun isValidPassword(password: String): Boolean = password.length >= 6 && password.any { it.isDigit() }

    fun validateAndSignUp() {
        emailError = !isValidEmail(email)
        usernameError = username.isBlank()
        passwordError = !isValidPassword(password)
        retypePasswordError = password != retypePassword
        if (emailError || usernameError || passwordError || retypePasswordError) {
            errorMessage = when {
                emailError -> "Invalid email address."
                usernameError -> "Username cannot be empty."
                passwordError -> "Password must be at least 6 characters and contain a number."
                retypePasswordError -> "Passwords do not match."
                else -> "Please fill in all fields."
            }
            showError = true
            coroutineScope.launch {
                delay(5000)
                showError = false
            }
            return
        }
        // Check if username exists in Firestore
        db.collection("users").whereEqualTo("username", username).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                errorMessage = "Username already exists."
                showError = true
                usernameError = true
                coroutineScope.launch {
                    delay(5000)
                    showError = false
                }
            } else {
                // Username is unique, create user
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val user = hashMapOf("username" to username, "email" to email)
                            if (userId != null) {
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        // Show success and redirect to home (logged in)
                                        errorMessage = "Signup successful! Redirecting..."
                                        showError = true
                                        coroutineScope.launch {
                                            delay(1500)
                                            showError = false
                                            onSignUpSuccess()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = e.localizedMessage ?: "Failed to save user."
                                        showError = true
                                        coroutineScope.launch {
                                            delay(5000)
                                            showError = false
                                        }
                                    }
                            }
                        } else {
                            errorMessage = task.exception?.localizedMessage ?: "Sign up failed."
                            showError = true
                            coroutineScope.launch {
                                delay(5000)
                                showError = false
                            }
                        }
                    }
            }
        }.addOnFailureListener { e ->
            errorMessage = e.localizedMessage ?: "Failed to check username."
            showError = true
            coroutineScope.launch {
                delay(5000)
                showError = false
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
            Text("Sign Up", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                },
                label = { Text("Email") },
                isError = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = false
                },
                label = { Text("Username") },
                isError = usernameError,
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = retypePassword,
                onValueChange = {
                    retypePassword = it
                    retypePasswordError = false
                },
                label = { Text("Re-Type Password") },
                isError = retypePasswordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { validateAndSignUp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
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
                Text("Already have an account? ")
                Text(
                    text = "Login",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToLogin)
                        .padding(2.dp)
                )
            }
        }
    }
}

