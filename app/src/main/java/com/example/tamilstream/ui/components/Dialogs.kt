package com.example.tamilstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.Channel
import com.example.tamilstream.data.model.Movie
import com.example.tamilstream.data.model.SubscriptionPlan
import com.example.tamilstream.ui.theme.*

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode2
import coil.compose.AsyncImage
import android.content.Context
import com.example.tamilstream.data.model.ManualUpiConfig
import com.example.tamilstream.data.model.ManualPaymentRequest

@Composable
fun EditChannelDialog(
    channelToEdit: Channel?,
    onDismiss: () -> Unit,
    onSave: (Channel) -> Unit
) {
    var name by remember { mutableStateOf(channelToEdit?.cleanName ?: "") }
    var group by remember { mutableStateOf(channelToEdit?.group ?: "News") }
    var resolution by remember { mutableStateOf(channelToEdit?.resolution ?: "1080P") }
    var logoUrl by remember { mutableStateOf(channelToEdit?.logo ?: "") }
    var streamUrl by remember { mutableStateOf(channelToEdit?.url ?: "") }
    var fallbackUrl by remember { mutableStateOf(channelToEdit?.fallbackUrl ?: "") }
    var isChannelEnabled by remember { mutableStateOf(channelToEdit?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (channelToEdit == null) "Add Custom Channel" else "Edit Channel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Channel Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_channel_name_input")
                )
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("Primary HLS / DASH / MP4 Stream URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_channel_url_input")
                )
                OutlinedTextField(
                    value = fallbackUrl,
                    onValueChange = { fallbackUrl = it },
                    label = { Text("Authorized Backup / Fallback URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("Channel Logo URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = { group = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = resolution,
                        onValueChange = { resolution = it },
                        label = { Text("Resolution") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isChannelEnabled,
                        onCheckedChange = { isChannelEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Amber500)
                    )
                    Text("Channel Enabled for Viewers", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && streamUrl.isNotBlank()) {
                        val ch = Channel(
                            id = channelToEdit?.id ?: "custom_${System.currentTimeMillis()}",
                            name = name,
                            cleanName = name,
                            group = group,
                            resolution = resolution,
                            logo = if (logoUrl.isNotBlank()) logoUrl else "https://jiotvimages.cdn.jio.com/dare_images/images/aryatvtamil.png",
                            url = streamUrl,
                            fallbackUrl = if (fallbackUrl.isNotBlank()) fallbackUrl else null,
                            enabled = isChannelEnabled,
                            isCustom = true
                        )
                        onSave(ch)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("save_channel_confirm_btn")
            ) {
                Text("Save Channel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Amber500)
                Text(
                    text = "Admin Access Portal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Please enter administrator security credentials or PIN code to enter the control center.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        error = false
                    },
                    label = { Text("Admin PIN (e.g. 1234 or admin123)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = error,
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )
                if (error) {
                    Text(
                        text = "Invalid credential. Try 1234 or admin123",
                        color = LiveRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = onLogin(pin)
                    if (!success) error = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("admin_login_submit_btn")
            ) {
                Text("Authenticate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}


@Composable
fun CheckoutDialog(
    plan: SubscriptionPlan,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = "VIP", tint = Amber500)
                Text(
                    text = "Checkout - ${plan.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Amount Due", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(text = "₹${plan.priceInr} INR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Amber500)
                        }
                        Text(text = plan.billingPeriodLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                Text(text = "Payment Method:", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                // Single Payment Method Option: IMB Payment Gateway
                Surface(
                    color = Amber500.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Amber500),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = true,
                            onClick = {},
                            colors = RadioButtonDefaults.colors(selectedColor = Amber500)
                        )
                        Icon(Icons.Default.CreditCard, contentDescription = "IMB Gateway", tint = Amber500)
                        Column {
                            Text(
                                text = "IMB Payment Gateway",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Official Instant Checkout (UPI, Cards & NetBanking)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPayment,
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("checkout_confirm_pay_btn")
            ) {
                Text("Proceed via IMB Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun ManualUpiCheckoutDialog(
    plan: SubscriptionPlan,
    config: ManualUpiConfig,
    isSubmitting: Boolean,
    submissionMessage: String?,
    activeRequest: ManualPaymentRequest?,
    onDismiss: () -> Unit,
    onSubmitPayment: (String, String) -> Unit
) {
    val context = LocalContext.current
    var paymentIdentifier by remember { mutableStateOf("") }
    var screenshotUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = "UPI", tint = Amber500)
                Text(
                    text = "Manual UPI Payment — ${plan.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plan Selected:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(plan.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount Due:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("₹${plan.priceInr} INR", fontWeight = FontWeight.Bold, color = Amber500)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Validity:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("${plan.durationDays} Days", fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }
                }

                if (activeRequest != null) {
                    Surface(
                        color = if (activeRequest.status == "ACCEPTED") LiveGreen.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Request Status: ${activeRequest.status}",
                                fontWeight = FontWeight.Bold,
                                color = if (activeRequest.status == "ACCEPTED") LiveGreen else Amber500
                            )
                            Text(
                                text = "UTR / ID: ${activeRequest.paymentIdentifier}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                text = if (activeRequest.status == "ACCEPTED") "Your VIP membership is active!" else "Awaiting admin verification. Verification typically takes 15-30 minutes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    if (config.qrCodeUrl.isNotBlank()) {
                        AsyncImage(
                            model = config.qrCodeUrl,
                            contentDescription = "UPI QR Code",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        )
                    }

                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Paytm UPI ID:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(config.paytmUpiId, fontWeight = FontWeight.Bold, color = Amber500, fontSize = 14.sp)
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("UPI ID", config.paytmUpiId)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "UPI ID copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Copy ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (config.instructions.isNotBlank()) {
                        Text(
                            text = config.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = paymentIdentifier,
                        onValueChange = { paymentIdentifier = it },
                        label = { Text("UPI Transaction ID / UTR / Order ID *") },
                        placeholder = { Text("e.g. UTR4829101923") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_upi_utr_input")
                    )

                    OutlinedTextField(
                        value = screenshotUrl,
                        onValueChange = { screenshotUrl = it },
                        label = { Text("Payment Proof Screenshot URL (Optional)") },
                        placeholder = { Text("https://imgur.com/screenshot.jpg") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_upi_screenshot_input")
                    )

                    if (submissionMessage != null) {
                        Text(
                            text = submissionMessage,
                            color = LiveGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (activeRequest == null) {
                Button(
                    onClick = {
                        if (paymentIdentifier.isNotBlank()) {
                            onSubmitPayment(paymentIdentifier.trim(), screenshotUrl.trim())
                        }
                    },
                    enabled = !isSubmitting && paymentIdentifier.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                    modifier = Modifier.testTag("manual_upi_submit_btn")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ObsidianBlack, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submitting...")
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Submit Payment for Verification")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (activeRequest != null) "Done" else "Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun EditSubscriptionPlanDialog(
    planToEdit: SubscriptionPlan?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionPlan) -> Unit
) {
    var name by remember { mutableStateOf(planToEdit?.name ?: "") }
    var priceText by remember { mutableStateOf((planToEdit?.priceInr ?: 499).toString()) }
    var daysText by remember { mutableStateOf((planToEdit?.durationDays ?: 30).toString()) }
    var billingLabel by remember { mutableStateOf(planToEdit?.billingPeriodLabel ?: "30 Days") }
    var description by remember { mutableStateOf(planToEdit?.description ?: "") }
    var badgeText by remember { mutableStateOf(planToEdit?.badge ?: "") }
    var featuresText by remember { mutableStateOf(planToEdit?.features?.joinToString(", ") ?: "Live TV Channels, HD Playback, Multiple Devices") }
    var displayOrderText by remember { mutableStateOf((planToEdit?.displayOrder ?: 0).toString()) }
    var isFeatured by remember { mutableStateOf(planToEdit?.isFeatured ?: false) }
    var isActive by remember { mutableStateOf(planToEdit?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (planToEdit == null) "Create VIP / Premium Plan" else "Edit Subscription Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plan Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (₹ INR) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it },
                        label = { Text("Validity Days *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = billingLabel,
                        onValueChange = { billingLabel = it },
                        label = { Text("Billing Label") },
                        placeholder = { Text("e.g. 30 Days") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = displayOrderText,
                        onValueChange = { displayOrderText = it },
                        label = { Text("Sort Order") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = badgeText,
                    onValueChange = { badgeText = it },
                    label = { Text("Badge Label (Optional)") },
                    placeholder = { Text("e.g. Most Popular / VIP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = featuresText,
                    onValueChange = { featuresText = it },
                    label = { Text("Features (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active / Available for Purchase", color = TextPrimary)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = LiveGreen)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Plan (Highlighted)", color = TextPrimary)
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Amber500)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedFeatures = featuresText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val plan = SubscriptionPlan(
                            id = planToEdit?.id ?: "plan_${System.currentTimeMillis()}",
                            name = name.trim(),
                            slug = name.lowercase().replace(" ", "-"),
                            priceInr = priceText.toIntOrNull() ?: 499,
                            durationDays = daysText.toIntOrNull() ?: 30,
                            billingPeriodLabel = billingLabel.ifBlank { "30 Days" },
                            description = description.trim(),
                            features = if (parsedFeatures.isNotEmpty()) parsedFeatures else listOf("Live TV Channels", "HD Playback"),
                            isFeatured = isFeatured,
                            isActive = isActive,
                            displayOrder = displayOrderText.toIntOrNull() ?: 0,
                            badge = badgeText.ifBlank { null },
                            createdAt = planToEdit?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(plan)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("save_subscription_plan_btn")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = DarkSurface
    )
}


@Composable
fun PremiumLockAlert(
    movie: Movie,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = "VIP", tint = Amber500)
                Text(
                    text = "VIP Exclusive Content",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "\"${movie.title}\" is part of the TamilStream VIP Catalog.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Upgrade to TamilStream Premium to enjoy 4K Ultra HD ad-free playback across all your devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("premium_alert_upgrade_btn")
            ) {
                Text("View VIP Passes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun ForgotPasswordDialog(
    isLoading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Amber500)
                Text("Password Recovery", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (successMessage != null) {
                    Surface(
                        color = LiveGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = successMessage,
                            color = LiveGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Enter your registered email address and we will dispatch a secure Firebase password reset verification link.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Registered Email Address") },
                        placeholder = { Text("name@example.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("forgot_password_email_input")
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage, color = LiveRed, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (successMessage != null) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack)
                ) {
                    Text("Done")
                }
            } else {
                Button(
                    onClick = { onSubmit(email) },
                    enabled = !isLoading && email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                    modifier = Modifier.testTag("forgot_password_submit_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ObsidianBlack, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sending...")
                    } else {
                        Text("Send Reset Link")
                    }
                }
            }
        },
        dismissButton = {
            if (successMessage == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurface
    )
}
