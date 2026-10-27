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
fun AdminLoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onAdminLogin: (String, String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminIdentifier by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("admin_login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield & Gold Emblem
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Amber500, Amber700)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Shield",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Control Portal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )

            Text(
                text = "Live Tamil TV Administrative Authentication Gateway",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Amber500,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Alert Banner
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
                        .testTag("admin_login_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GppBad,
                            contentDescription = "Access Denied",
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
                        IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = LiveRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Admin Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Amber500.copy(alpha = 0.4f)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Admin ID or Email
                    OutlinedTextField(
                        value = adminIdentifier,
                        onValueChange = {
                            adminIdentifier = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Admin User ID or Email") },
                        placeholder = { Text("e.g. sanjeevidaa@gmail.com or admin") },
                        leadingIcon = {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Amber500)
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
                            .testTag("admin_identifier_input")
                    )

                    // Password
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = {
                            adminPassword = it
                            if (errorMessage != null) onClearError()
                        },
                        label = { Text("Admin Secret Key / Password") },
                        placeholder = { Text("Enter configured admin password") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Amber500)
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
                            .testTag("admin_password_input")
                    )

                    // Remember Me & Forgot Password
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
                                )
                            )
                            Text("Remember Device", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }

                        TextButton(onClick = onForgotPasswordClick) {
                            Text("Forgot Key?", color = Amber500, fontSize = 12.sp)
                        }
                    }

                    // Authenticate Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onAdminLogin(adminIdentifier, adminPassword, rememberMe)
                        },
                        enabled = !isLoading && adminIdentifier.isNotBlank() && adminPassword.isNotBlank(),
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
                            .testTag("admin_login_submit_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying Security Credentials...")
                        } else {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Authenticate Administrator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Back to Normal App
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("admin_back_to_app_btn")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Return to Live Tamil TV App", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
