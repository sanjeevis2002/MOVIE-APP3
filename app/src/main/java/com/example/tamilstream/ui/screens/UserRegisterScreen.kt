package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.ui.theme.*

@Composable
fun UserRegisterScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (fullName: String, username: String, email: String, pass: String, confirm: String, terms: Boolean) -> Unit,
    onNavigateLogin: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Real-time validation checks
    val isUsernameValid = username.length in 3..30 && username.matches(Regex("^[a-zA-Z0-9_.]+$"))
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordLengthValid = password.length >= 6
    val doPasswordsMatch = password.isNotEmpty() && password == confirmPassword
    val isFormComplete = fullName.isNotBlank() && isUsernameValid && isEmailValid && isPasswordLengthValid && doPasswordsMatch && acceptedTerms

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("user_register_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Emblem
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Amber400, Amber600)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Register",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )

            Text(
                text = "Join Live Tamil TV for non-stop regional entertainment",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner if present
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = LiveRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(LiveRed),
                        width = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("register_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = LiveRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = LiveRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = LiveRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                    width = 0.5.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Sanjeevi Kumar") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Amber500)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_fullname_input")
                    )

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it.trim()
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Username (@handle)") },
                        placeholder = { Text("e.g. sanjeevi2025") },
                        leadingIcon = {
                            Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = Amber500)
                        },
                        singleLine = true,
                        supportingText = {
                            if (username.isNotEmpty() && !isUsernameValid) {
                                Text("3-30 chars (letters, digits, _ and . only)", color = LiveRed, fontSize = 11.sp)
                            }
                        },
                        isError = username.isNotEmpty() && !isUsernameValid,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_username_input")
                    )

                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it.trim()
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("name@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Amber500)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        supportingText = {
                            if (email.isNotEmpty() && !isEmailValid) {
                                Text("Please enter a valid email address", color = LiveRed, fontSize = 11.sp)
                            }
                        },
                        isError = email.isNotEmpty() && !isEmailValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input")
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Password (Min 6 chars)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Amber500)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        supportingText = {
                            if (password.isNotEmpty() && !isPasswordLengthValid) {
                                Text("Password must be at least 6 characters", color = LiveRed, fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Amber500)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                                Icon(
                                    imageVector = if (isConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                                Text("Passwords do not match", color = LiveRed, fontSize = 11.sp)
                            }
                        },
                        isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirm_password_input")
                    )

                    // Terms Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Amber500,
                                checkmarkColor = ObsidianBlack
                            ),
                            modifier = Modifier.testTag("register_terms_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I accept the Terms of Service & Privacy Policy",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }

                    // Create Account Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onRegister(fullName, username, email, password, confirmPassword, acceptedTerms)
                        },
                        enabled = !isLoading && isFormComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber500,
                            contentColor = ObsidianBlack,
                            disabledContainerColor = DarkSurfaceElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_submit_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating Account...")
                        } else {
                            Text(
                                text = "Create Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Link to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already registered?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                TextButton(
                    onClick = onNavigateLogin,
                    modifier = Modifier.testTag("register_navigate_login_btn")
                ) {
                    Text(
                        text = "Sign In",
                        color = Amber500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
