package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun UserLoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    authPromptReason: String? = null,
    onLogin: (String, String, Boolean) -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateAdminLogin: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onCancel: () -> Unit = onContinueAsGuest,
    onForgotPasswordClick: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("user_login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Amber400, Amber600)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Logo",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )

            Text(
                text = "Sign in to access Live Tamil TV & Blockbuster Cinema",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Protected Playback or Feature Explanation Banner
            if (!authPromptReason.isNullOrBlank()) {
                Surface(
                    color = Amber500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Amber500.copy(alpha = 0.5f)),
                        width = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("login_auth_prompt_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sign-In Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = authPromptReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

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
                        .testTag("login_error_banner")
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

            // Input Card
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email or Username
                    OutlinedTextField(
                        value = emailOrUsername,
                        onValueChange = {
                            emailOrUsername = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Email Address or Username") },
                        placeholder = { Text("e.g. username or user@example.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Identifier",
                                tint = Amber500
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = Amber500,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input")
                    )

                    // Password with Show/Hide toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = Amber500
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("login_toggle_password_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLogin(emailOrUsername, password, rememberMe)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = Amber500,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Remember Me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Amber500,
                                    checkmarkColor = ObsidianBlack
                                ),
                                modifier = Modifier.testTag("login_remember_me_checkbox")
                            )
                            Text(
                                text = "Remember Me",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }

                        TextButton(
                            onClick = onForgotPasswordClick,
                            modifier = Modifier.testTag("login_forgot_password_btn")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = Amber500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Login Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onLogin(emailOrUsername, password, rememberMe)
                        },
                        enabled = !isLoading && emailOrUsername.isNotBlank() && password.isNotBlank(),
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
                            .testTag("login_submit_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Authenticating...")
                        } else {
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Register Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                TextButton(
                    onClick = onNavigateRegister,
                    modifier = Modifier.testTag("login_navigate_register_btn")
                ) {
                    Text(
                        text = "Create Account",
                        color = Amber500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cancel & Return to Browsing Option
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("login_cancel_browsing_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel & Return to Browsing", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dedicated Admin Access Link
            TextButton(
                onClick = onNavigateAdminLogin,
                modifier = Modifier.testTag("login_admin_portal_link")
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Admin",
                    tint = Amber600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Administrator Control Center",
                    color = Amber600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
