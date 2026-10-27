package com.example.tamilstream.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.User
import com.example.tamilstream.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    currentUser: User,
    onNavigateHome: () -> Unit,
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(Unit) {
        delay(1200)
        onNavigateHome()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Logo Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Amber400, Amber600, Amber700)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Live Tamil TV Logo",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LIVE TAMIL ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = "TV",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Amber500,
                        letterSpacing = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "PREMIUM LIVE IPTV & CINEMA",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 3.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Loading indicator
            CircularProgressIndicator(
                color = Amber500,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }

        // Quick Skip option at bottom
        TextButton(
            onClick = {
                if (currentUser.id != "user_guest" && currentUser.isActive) onNavigateHome() else onNavigateLogin()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .testTag("splash_skip_btn")
        ) {
            Text("Tap to Enter", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
