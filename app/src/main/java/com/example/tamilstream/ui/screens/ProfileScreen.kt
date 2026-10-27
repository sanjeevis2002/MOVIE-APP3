package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.User
import com.example.tamilstream.data.model.UserSubscription
import com.example.tamilstream.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    user: User,
    subscription: UserSubscription?,
    onNavigateLogin: () -> Unit = {},
    onNavigateWatchlist: () -> Unit = {},
    onNavigateEpisodes: () -> Unit = {},
    onUpdateProfile: (displayName: String, avatar: String) -> Unit,
    onNavigateSubscriptions: () -> Unit,
    onNavigateAdmin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user.displayName) }
    var editAvatar by remember { mutableStateOf(user.avatar) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Header Card
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Edit Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Amber400, Amber600)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user.displayName.firstOrNull() ?: 'U').uppercase(),
                        color = ObsidianBlack,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Amber500,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Role Badge
                    Surface(
                        color = if (user.isAdmin) Amber500.copy(alpha = 0.2f) else DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (user.isAdmin) Icons.Default.Shield else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (user.isAdmin) Amber500 else TextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (user.isAdmin) "ADMINISTRATOR" else "MEMBER",
                                color = if (user.isAdmin) Amber500 else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // VIP Status Badge
                    Surface(
                        color = if (user.isPremium) Purple500.copy(alpha = 0.2f) else DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (user.isPremium) Amber500 else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (user.isPremium) "VIP PASS ACTIVE" else "STANDARD VIEWER",
                                color = if (user.isPremium) Amber500 else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (user.isGuest) {
                    Button(
                        onClick = onNavigateLogin,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber500,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("profile_guest_login_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In or Create Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            editName = user.displayName
                            editAvatar = user.avatar
                            showEditProfileDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("profile_edit_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile Details", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subscription Status Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIP Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    TextButton(onClick = onNavigateSubscriptions) {
                        Text(if (user.isPremium) "Change Plan" else "Upgrade to VIP", color = Amber500)
                    }
                }

                if (subscription != null) {
                    Text(
                        text = "Active Plan: ${subscription.planName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Amber400,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Valid Until: ${subscription.expiryDate} • Quality: ${subscription.videoQuality}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "You are currently on the Free Regional pass with standard live channels. Upgrade to unlock 4K Tamil cinema releases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content & Library Navigation Section (Watchlist & Episodes)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                width = 0.5.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Library & Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Quick Access",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Watchlist Menu Option
                    OutlinedButton(
                        onClick = onNavigateWatchlist,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("profile_watchlist_menu_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Watchlist",
                            tint = Amber500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Watchlist", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    // Episodes Menu Option (New!)
                    Button(
                        onClick = onNavigateEpisodes,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("profile_episodes_menu_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber500.copy(alpha = 0.15f),
                            contentColor = Amber400
                        ),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Amber500.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Episodes",
                            tint = Amber500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Episodes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Portal Quick Action (if Admin)
        if (user.isAdmin) {
            Button(
                onClick = onNavigateAdmin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_open_admin_btn")
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Control Portal Dashboard", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Account Details Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account Metadata", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                HorizontalDivider(color = DarkBorder)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("User ID (UID):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(user.uid.take(16) + "...", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(user.accountStatus.uppercase(), style = MaterialTheme.typography.bodySmall, color = LiveGreen, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Registered:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.createdAt))
                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (user.isGuest) {
            Button(
                onClick = onNavigateLogin,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = ObsidianBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_guest_signin_footer_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Sign In")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign In to Access All Features", fontWeight = FontWeight.Bold)
            }
        } else {
            // Secure Logout Button
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LiveRed.copy(alpha = 0.5f))
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Account", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile Details", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAvatar,
                        onValueChange = { editAvatar = it },
                        label = { Text("Avatar Image URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editAvatar)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Confirm Sign Out", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of Live Tamil TV?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = ObsidianBlack)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
