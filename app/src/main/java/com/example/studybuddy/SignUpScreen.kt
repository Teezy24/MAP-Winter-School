package com.example.studybuddy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var retypePassword by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf(false) }
    var usernameError by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf(false) }
    var retypePasswordError by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // Password visibility toggles
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var retypePasswordVisible by rememberSaveable { mutableStateOf(false) }
    // Loading state
    var isLoading by rememberSaveable { mutableStateOf(false) }

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
        isLoading = true

        // First, check if the email is already in use before creating the user
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { emailDocs ->
                if (!emailDocs.isEmpty) {
                    errorMessage = "This email is already registered. Please use another email or log in."
                    showError = true
                    isLoading = false
                    coroutineScope.launch {
                        delay(5000)
                        showError = false
                    }
                } else {
                    // --- FIX: Allow username check without requiring authentication ---
                    // Instead of checking username before sign up (which requires read permission),
                    // first create the user (so request.auth != null), then check username and write user doc.

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.currentUser?.reload()?.addOnCompleteListener {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        // Now user is authenticated, check if username exists
                                        db.collection("users").whereEqualTo("username", username).get()
                                            .addOnSuccessListener { docs ->
                                                if (!docs.isEmpty) {
                                                    // Username exists, delete the just-created user and show error
                                                    auth.currentUser?.delete()
                                                    errorMessage = "Username already exists."
                                                    showError = true
                                                    usernameError = true
                                                    isLoading = false
                                                    coroutineScope.launch {
                                                        delay(5000)
                                                        showError = false
                                                    }
                                                } else {
                                                    // Username is unique, save user doc
                                                    val user = hashMapOf("username" to username, "email" to email)
                                                    db.collection("users").document(userId).set(user)
                                                        .addOnSuccessListener {
                                                            onSignUpSuccess()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            errorMessage = e.localizedMessage ?: "Failed to save user."
                                                            showError = true
                                                            isLoading = false
                                                            coroutineScope.launch {
                                                                delay(5000)
                                                                showError = false
                                                            }
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = e.localizedMessage ?: "Failed to check username."
                                                showError = true
                                                isLoading = false
                                                coroutineScope.launch {
                                                    delay(5000)
                                                    showError = false
                                                }
                                            }
                                    }
                                }
                            } else {
                                val errorMsg = task.exception?.localizedMessage
                                if (errorMsg?.contains("email address is already in use") == true) {
                                    errorMessage = "This email is already registered. Please use another email or log in."
                                } else {
                                    errorMessage = errorMsg ?: "Sign up failed."
                                }
                                showError = true
                                isLoading = false
                                coroutineScope.launch {
                                    delay(5000)
                                    showError = false
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                errorMessage = e.localizedMessage ?: "Failed to check email."
                showError = true
                isLoading = false
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                visualTransformation = if (retypePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (retypePasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(
                        onClick = { retypePasswordVisible = !retypePasswordVisible }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (retypePasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { validateAndSignUp() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign Up")
                }
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToLogin)
                        .padding(2.dp)
                )
            }
        }
    }
}
