package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.SubscriptionPlan
import com.example.tamilstream.data.model.User
import com.example.tamilstream.data.model.UserSubscription
import com.example.tamilstream.ui.theme.*

@Composable
fun SubscriptionScreen(
    user: User,
    activeSubscription: UserSubscription?,
    plans: List<SubscriptionPlan>,
    onSelectPlan: (SubscriptionPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayPlans = remember(plans, activeSubscription) {
        plans.filter { it.isActive || it.id == activeSubscription?.planId }.sortedBy { it.displayOrder }
    }

    LazyColumn(
        modifier = modifier
            .testTag("subscription_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF311000), Color(0xFF1E1405), DarkSurface)
                        )
                    )
                    .border(1.dp, Amber500.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "VIP",
                            tint = Amber500,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "TamilStream VIP Pass",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Amber500
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Unlock 4K Ultra HD Streaming, 60+ Live Tamil Satellite Channels, and Day-1 Digital Releases without advertisements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (user.isPremium && activeSubscription != null) {
                        Surface(
                            color = LiveGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LiveGreen)
                            )
                        ) {
                            Text(
                                text = "ACTIVE SUBSCRIPTION: ${activeSubscription.planName} (${activeSubscription.expiryDate})",
                                color = LiveGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Plans Header
        item {
            Text(
                text = "Choose Your Membership Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Plan Cards
        items(displayPlans) { plan ->
            val isCurrentPlan = activeSubscription?.planId == plan.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_card_${plan.slug}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = if (plan.isFeatured) CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Amber500),
                    width = 1.5.dp
                ) else CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                    width = 0.5.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Plan Name & Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (plan.isFeatured) Amber500 else TextPrimary
                            )
                            Text(
                                text = "${plan.billingPeriodLabel} Access",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (plan.badge != null) {
                            Surface(
                                color = Amber500,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = plan.badge,
                                    color = ObsidianBlack,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${plan.priceInr}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = " / ${plan.billingPeriodLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Features checklist
                    plan.features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Included",
                                tint = Amber500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CTA Button
                    Button(
                        onClick = { onSelectPlan(plan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_plan_btn_${plan.slug}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plan.isFeatured) Amber500 else DarkSurfaceElevated,
                            contentColor = if (plan.isFeatured) ObsidianBlack else TextPrimary
                        )
                    ) {
                        Text(
                            text = if (isCurrentPlan) "Active Plan" else "Subscribe for ₹${plan.priceInr}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
