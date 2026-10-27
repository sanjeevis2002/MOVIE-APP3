package com.example.tamilstream.data.model

data class Channel(
    val id: String = "",
    val name: String,
    val cleanName: String = name,
    val tvgName: String = "",
    val resolution: String = "1080P",
    val logo: String,
    val group: String = "General",
    val url: String,
    val fallbackUrl: String? = null,
    val enabled: Boolean = true,
    val isFavorite: Boolean = false,
    val isFeatured: Boolean = false,
    val isCustom: Boolean = false,
    val lastCheckStatus: String? = null,
    val lastCheckCode: Int? = null,
    val lastCheckedTimestamp: Long? = null,
    val addedAt: Long = System.currentTimeMillis()
)

data class Movie(
    val id: String,
    val title: String,
    val slug: String = "",
    val poster: String,
    val thumbnail: String = poster,
    val backdrop: String = thumbnail,
    val description: String,
    val synopsis: String = description,
    val genre: List<String> = emptyList(),
    val category: String = genre.firstOrNull() ?: "Action",
    val language: String = "Tamil",
    val releaseYear: Int = 2024,
    val ageRating: String = "U/A 16+",
    val cast: List<String> = emptyList(),
    val director: String = "",
    val duration: Int = 140, // in minutes
    val videoUrl: String,
    val status: String = "Published", // "Published", "Unpublished", "Draft"
    val isEnabled: Boolean = true,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isPremium: Boolean = false,
    val views: Long = 0,
    val rating: Double = 8.5,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "admin"
) {
    val isVisibleToUsers: Boolean get() = status.equals("Published", ignoreCase = true) && isEnabled
}

data class TVShow(
    val id: String,
    val title: String,
    val description: String,
    val poster: String,
    val banner: String,
    val language: String = "Tamil",
    val genre: List<String> = emptyList(),
    val releaseYear: Int = 2024,
    val isPremium: Boolean = false,
    val rating: Double = 8.0,
    val totalSeasons: Int = 1
)

data class LiveStream(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val streamUrl: String,
    val status: String = "live",
    val language: String = "Tamil",
    val category: String = "News",
    val isFeatured: Boolean = false
)

data class User(
    val id: String = "user_guest",
    val uid: String = id,
    val name: String = "Guest Viewer",
    val displayName: String = name,
    val username: String = "guest",
    val email: String = "guest@tamilstream.app",
    val avatar: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80",
    val role: String = "user", // "user" or "admin"
    val accountStatus: String = "active", // "active" or "disabled"
    val isPremium: Boolean = false,
    val emailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean get() = role.equals("admin", ignoreCase = true)
    val isActive: Boolean get() = accountStatus.equals("active", ignoreCase = true)
    val isGuest: Boolean get() = id == "user_guest" || uid == "user_guest"
    val isAuthenticated: Boolean get() = !isGuest && isActive
}

data class AppSettings(
    val appName: String = "Tamil TV",
    val logoUrl: String = "",
    val theme: String = "obsidian",
    val maintenanceMode: Boolean = false,
    val guestBrowsingEnabled: Boolean = true
)

data class AdminActivityLog(
    val id: String = "",
    val adminUid: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class CategoryItem(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = "",
    val enabled: Boolean = true,
    val sortOrder: Int = 0
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String, val code: String? = null) : AuthResult()
}

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val slug: String,
    val priceInr: Int,
    val durationDays: Int,
    val billingPeriodLabel: String,
    val description: String,
    val features: List<String>,
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val videoQuality: String = "Full HD 1080p",
    val maxScreens: Int = 2,
    val badge: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserSubscription(
    val id: String,
    val planId: String,
    val planName: String,
    val amountInr: Int,
    val startDate: String,
    val expiryDate: String,
    val status: String = "active",
    val videoQuality: String = "4K Ultra HD"
)

data class WatchlistItem(
    val id: String,
    val contentId: String,
    val contentType: String, // "movie" or "channel"
    val title: String,
    val poster: String,
    val rating: Double? = null,
    val releaseYear: Int? = null,
    val category: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

data class WatchHistoryItem(
    val id: String,
    val contentId: String,
    val contentType: String,
    val title: String,
    val thumbnail: String,
    val progressSeconds: Long = 0,
    val durationSeconds: Long = 0,
    val watchedAt: Long = System.currentTimeMillis()
)

data class PlaybackItem(
    val id: String,
    val title: String,
    val streamUrl: String,
    val fallbackUrl: String? = null,
    val thumbnail: String,
    val category: String,
    val isLive: Boolean = true,
    val durationMinutes: Int = 0
)

data class PlaybackDiagnostic(
    val channelId: String,
    val channelName: String,
    val host: String,
    val protocol: String,
    val errorCode: String,
    val errorMessage: String,
    val httpStatus: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Series(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val mainThumbnailUrl: String,
    val description: String = "",
    val status: String = "Published", // "Draft", "Published", "Unpublished"
    val isEnabled: Boolean = true,
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "admin"
) {
    val isPublished: Boolean get() = status.equals("Published", ignoreCase = true)
    val isVisibleToUsers: Boolean get() = isPublished && isEnabled
}

data class Episode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val seriesId: String = "",
    val seriesName: String = "Tamil TV Serial",
    val episodeNumber: Int = 1,
    val seasonNumber: Int = 1,
    val title: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val description: String = "",
    val status: String = "Published", // "Draft", "Published", "Unpublished"
    val isFeatured: Boolean = false,
    val isEnabled: Boolean = true,
    val durationMinutes: Int = 24,
    val views: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "admin"
) {
    val isPublished: Boolean get() = status.equals("Published", ignoreCase = true)
    val isVisibleToUsers: Boolean get() = isPublished && isEnabled
}

data class AdConfig(
    val id: String = "global_advertisement",
    val htmlContent: String = "",
    val enabled: Boolean = true,
    val topEnabled: Boolean = true,
    val bottomEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "admin",
    val version: Long = 1
) {
    val isVisible: Boolean get() = enabled && htmlContent.isNotBlank()
    val isTopVisible: Boolean get() = isVisible && topEnabled
    val isBottomVisible: Boolean get() = isVisible && bottomEnabled
}

data class ManualUpiConfig(
    val id: String = "manual_upi_config",
    val enabled: Boolean = true,
    val paytmUpiId: String = "merchant.paytm@paytm",
    val qrCodeUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&auto=format&fit=crop&q=80",
    val instructions: String = "1. Open your GPay, PhonePe, Paytm or any UPI app.\n2. Scan the QR code or pay to the Paytm UPI ID.\n3. Pay the exact plan amount.\n4. Enter your UPI Transaction ID (UTR) or Order ID below and upload payment screenshot.",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ManualPaymentRequest(
    val requestId: String = "req_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val userId: String,
    val userEmail: String,
    val userName: String,
    val planId: String,
    val planName: String,
    val amountInr: Int,
    val durationDays: Int,
    val paymentIdentifier: String, // Transaction ID / UTR or Order ID
    val screenshotUrl: String,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED"
    val submittedAt: Long = System.currentTimeMillis(),
    val adminId: String? = null,
    val decisionTimestamp: Long? = null,
    val rejectionReason: String? = null
)



