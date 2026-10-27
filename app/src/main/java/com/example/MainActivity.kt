package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.tamilstream.data.model.Channel
import com.example.tamilstream.data.model.Movie
import com.example.tamilstream.ui.components.*
import com.example.tamilstream.ui.screens.*
import com.example.tamilstream.ui.theme.*
import com.example.tamilstream.ui.viewmodel.Screen
import com.example.tamilstream.ui.viewmodel.TamilStreamViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TamilStreamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TamilStreamTheme {
                val uiState by viewModel.uiState.collectAsState()
                val channels by viewModel.channels.collectAsState()
                val movies by viewModel.movies.collectAsState()
                val episodes by viewModel.episodes.collectAsState()
                val plans by viewModel.plans.collectAsState()
                val watchlist by viewModel.watchlist.collectAsState()
                val watchHistory by viewModel.watchHistory.collectAsState()
                val user by viewModel.currentUser.collectAsState()
                val sub by viewModel.currentSubscription.collectAsState()
                val lastDiagnostic by viewModel.lastPlaybackDiagnostic.collectAsState()
                val activityLogs by viewModel.repository.adminActivityLogs.collectAsState()
                val movieCategories by viewModel.movieCategories.collectAsState()
                val seriesList by viewModel.seriesList.collectAsState()
                val adConfig by viewModel.adConfig.collectAsState()
                val manualUpiConfig by viewModel.manualUpiConfig.collectAsState()
                val manualPaymentRequests by viewModel.manualPaymentRequests.collectAsState()
                val isMasterPremiumEnabled by viewModel.isMasterPremiumEnabled.collectAsState()

                // Back navigation handling
                BackHandler(enabled = uiState.isFullScreen || (uiState.currentScreen != Screen.HOME && uiState.currentScreen != Screen.LOGIN)) {
                    if (uiState.isFullScreen) {
                        viewModel.toggleFullScreen()
                    } else {
                        when (uiState.currentScreen) {
                            Screen.REGISTER -> viewModel.navigateTo(Screen.LOGIN)
                            Screen.ADMIN_LOGIN -> viewModel.navigateTo(Screen.HOME)
                            Screen.EPISODES -> viewModel.navigateTo(Screen.PROFILE)
                            Screen.PROFILE, Screen.ADMIN, Screen.LIVE_TV, Screen.MOVIES, Screen.SEARCH, Screen.SUBSCRIPTIONS, Screen.WATCHLIST -> {
                                viewModel.navigateTo(Screen.HOME)
                            }
                            else -> {}
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_root_surface"),
                    color = ObsidianBlack
                ) {
                    when (uiState.currentScreen) {
                            Screen.SPLASH -> {
                                SplashScreen(
                                    currentUser = user,
                                    onNavigateHome = { viewModel.navigateTo(Screen.HOME) },
                                    onNavigateLogin = { viewModel.navigateTo(Screen.LOGIN) }
                                )
                            }

                            Screen.LOGIN -> {
                                UserLoginScreen(
                                    isLoading = uiState.isAuthLoading,
                                    errorMessage = uiState.authError,
                                    authPromptReason = uiState.authPromptReason,
                                    onLogin = { id, pass, rem ->
                                        viewModel.loginUser(id, pass, rem) {
                                            viewModel.navigateTo(Screen.HOME)
                                        }
                                    },
                                    onNavigateRegister = { viewModel.navigateTo(Screen.REGISTER) },
                                    onNavigateAdminLogin = { viewModel.navigateTo(Screen.ADMIN_LOGIN) },
                                    onContinueAsGuest = { viewModel.cancelAuth() },
                                    onCancel = { viewModel.cancelAuth() },
                                    onForgotPasswordClick = { viewModel.openForgotPassword() },
                                    onClearError = { viewModel.clearAuthError() }
                                )
                            }

                            Screen.REGISTER -> {
                                UserRegisterScreen(
                                    isLoading = uiState.isAuthLoading,
                                    errorMessage = uiState.authError,
                                    onRegister = { name, username, email, pass, confirm, terms ->
                                        viewModel.registerUser(name, username, email, pass, confirm, terms) {
                                            viewModel.navigateTo(Screen.HOME)
                                        }
                                    },
                                    onNavigateLogin = { viewModel.navigateTo(Screen.LOGIN) },
                                    onClearError = { viewModel.clearAuthError() }
                                )
                            }

                            Screen.ADMIN_LOGIN -> {
                                AdminLoginScreen(
                                    isLoading = uiState.isAuthLoading,
                                    errorMessage = uiState.authError,
                                    onAdminLogin = { id, pass, rem ->
                                        viewModel.loginAdmin(id, pass, rem) {
                                            viewModel.navigateTo(Screen.ADMIN)
                                        }
                                    },
                                    onNavigateBack = { viewModel.navigateTo(Screen.HOME) },
                                    onForgotPasswordClick = { viewModel.openForgotPassword() },
                                    onClearError = { viewModel.clearAuthError() }
                                )
                            }

                            else -> {
                                // Standard App Shell with TopBar and BottomNavBar
                                Scaffold(
                                    modifier = Modifier.fillMaxSize(),
                                    containerColor = ObsidianBlack,
                                    topBar = {
                                        if (!uiState.isFullScreen) {
                                            TopBar(
                                                user = user,
                                                onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                                                onAdminClick = {
                                                    if (user.isAdmin) {
                                                        viewModel.navigateTo(Screen.ADMIN)
                                                    } else {
                                                        viewModel.navigateTo(Screen.ADMIN_LOGIN)
                                                    }
                                                },
                                                onProfileClick = { viewModel.navigateTo(Screen.PROFILE) }
                                            )
                                        }
                                    },
                                    bottomBar = {
                                        if (!uiState.isFullScreen) {
                                            BottomNavBar(
                                                currentScreen = uiState.currentScreen,
                                                onNavigate = { viewModel.navigateTo(it) }
                                            )
                                        }
                                    }
                                ) { innerPadding ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(if (uiState.isFullScreen) PaddingValues(0.dp) else innerPadding)
                                    ) {
                                        // Docked or Fullscreen Video Player (persisted across orientation changes)
                                        if (uiState.activePlayback != null) {
                                            VideoPlayerView(
                                                playbackItem = uiState.activePlayback!!,
                                                isPlaying = uiState.isPlaying,
                                                isMuted = uiState.isMuted,
                                                isFullScreen = uiState.isFullScreen,
                                                onTogglePlay = { viewModel.togglePlayPause() },
                                                onToggleMute = { viewModel.toggleMute() },
                                                onToggleFullScreen = { viewModel.toggleFullScreen() },
                                                onClose = { viewModel.stopPlayback() },
                                                onNextChannel = { viewModel.playNextChannel() },
                                                onPreviousChannel = { viewModel.playPreviousChannel() },
                                                onRetry = { viewModel.retryPlayback() },
                                                onRecordDiagnostic = { viewModel.recordDiagnostic(it) },
                                                modifier = if (uiState.isFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
                                            )
                                        }

                                        // Placement A — Header Advertisement (Positioned immediately below Header)
                                        if (!uiState.isFullScreen && uiState.currentScreen != Screen.ADMIN && adConfig.isTopVisible) {
                                            AdBannerView(
                                                adConfig = adConfig,
                                                placementTag = "header_ad_banner"
                                            )
                                        }

                                        // Main Screen Body (hidden when fullscreen is active)
                                        if (!uiState.isFullScreen) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                when (uiState.currentScreen) {
                                                    Screen.HOME -> HomeScreen(
                                                        featuredMovie = movies.firstOrNull(),
                                                        liveChannels = channels,
                                                        movies = movies,
                                                        watchlist = watchlist,
                                                        watchHistory = watchHistory,
                                                        onPlayChannel = { viewModel.playChannel(it) },
                                                        onPlayMovie = { viewModel.playMovie(it) },
                                                        onSelectMovie = { viewModel.selectMovie(it) },
                                                        onToggleWatchlist = { viewModel.toggleWatchlistMovie(it) },
                                                        onNavigate = { viewModel.navigateTo(it) }
                                                    )

                                                    Screen.LIVE_TV -> LiveTvScreen(
                                                        channels = channels,
                                                        currentPlayingChannelId = uiState.activePlayback?.id,
                                                        activeCategory = uiState.activeCategory,
                                                        searchQuery = uiState.channelSearchQuery,
                                                        viewMode = uiState.channelViewMode,
                                                        showOnlyFavorites = uiState.showOnlyFavorites,
                                                        isRefreshing = uiState.isRefreshingPlaylist,
                                                        syncNotification = uiState.syncNotification,
                                                        onRefreshPlaylist = { viewModel.refreshPlaylist() },
                                                        onCategorySelect = { viewModel.setActiveCategory(it) },
                                                        onSearchChange = { viewModel.setChannelSearch(it) },
                                                        onToggleViewMode = { viewModel.toggleViewMode() },
                                                        onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                                                        onPlayChannel = { viewModel.playChannel(it) },
                                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                                        onAddChannelClick = { viewModel.openEditChannelModal(null) }
                                                    )

                                                    Screen.MOVIES -> MoviesScreen(
                                                        movies = movies,
                                                        watchlist = watchlist,
                                                        onSelectMovie = { viewModel.selectMovie(it) },
                                                        onToggleWatchlist = { viewModel.toggleWatchlistMovie(it) }
                                                    )

                                                    Screen.SEARCH -> SearchScreen(
                                                        searchQuery = uiState.searchQuery,
                                                        channels = channels,
                                                        movies = movies,
                                                        watchlistIds = watchlist.map { it.contentId }.toSet(),
                                                        onQueryChange = { viewModel.setUniversalSearch(it) },
                                                        onPlayChannel = { viewModel.playChannel(it) },
                                                        onSelectMovie = { viewModel.selectMovie(it) },
                                                        onToggleWatchlist = { viewModel.toggleWatchlistMovie(it) },
                                                        onToggleFavoriteChannel = { viewModel.toggleFavorite(it) }
                                                    )

                                                    Screen.SUBSCRIPTIONS -> SubscriptionScreen(
                                                        user = user,
                                                        activeSubscription = sub,
                                                        plans = plans,
                                                        onSelectPlan = { viewModel.openCheckout(it) }
                                                    )

                                                    Screen.WATCHLIST -> WatchlistScreen(
                                                        watchlist = watchlist,
                                                        watchHistory = watchHistory,
                                                        movies = movies,
                                                        channels = channels,
                                                        isGuest = user.isGuest,
                                                        onNavigateLogin = { viewModel.requestAuthForFeature("Sign in to sync your Watchlist and History across devices.") },
                                                        onPlayMovie = { viewModel.playMovie(it) },
                                                        onPlayChannel = { viewModel.playChannel(it) },
                                                        onRemoveFromWatchlist = { viewModel.toggleWatchlistMovie(it) },
                                                        onClearHistory = { viewModel.clearHistory() }
                                                    )

                                                     Screen.PROFILE -> ProfileScreen(
                                                        user = user,
                                                        subscription = sub,
                                                        onNavigateLogin = { viewModel.navigateTo(Screen.LOGIN) },
                                                        onNavigateWatchlist = { viewModel.navigateTo(Screen.WATCHLIST) },
                                                        onNavigateEpisodes = { viewModel.navigateTo(Screen.EPISODES) },
                                                        onUpdateProfile = { name, avatar -> viewModel.updateProfile(name, avatar) },
                                                        onNavigateSubscriptions = { viewModel.navigateTo(Screen.SUBSCRIPTIONS) },
                                                        onNavigateAdmin = {
                                                            if (user.isAdmin) {
                                                                viewModel.navigateTo(Screen.ADMIN)
                                                            } else {
                                                                viewModel.navigateTo(Screen.ADMIN_LOGIN)
                                                            }
                                                        },
                                                        onLogout = { viewModel.logout() }
                                                    )

                                                    Screen.EPISODES -> EpisodesScreen(
                                                        seriesList = seriesList,
                                                        episodes = episodes,
                                                        onPlayEpisode = { viewModel.playEpisode(it) },
                                                        onNavigateBack = { viewModel.navigateTo(Screen.HOME) }
                                                    )

                                                     Screen.ADMIN -> AdminPortalScreen(
                                                        channels = channels,
                                                        movies = movies,
                                                        movieCategories = movieCategories,
                                                        seriesList = seriesList,
                                                        episodes = episodes,
                                                        registeredUsers = uiState.registeredUsers,
                                                        activityLogs = activityLogs,
                                                        lastPlaybackDiagnostic = lastDiagnostic,
                                                        onAddOrEditChannel = { viewModel.openEditChannelModal(it) },
                                                        onDeleteChannel = { viewModel.deleteChannel(it) },
                                                        onAddMovie = { viewModel.addMovie(it) },
                                                        onAddOrUpdateMovie = { viewModel.saveMovie(it) },
                                                        onDeleteMovie = { viewModel.deleteMovie(it) },
                                                        onToggleMoviePublish = { viewModel.toggleMoviePublish(it) },
                                                        onToggleMovieFeatured = { viewModel.toggleMovieFeatured(it) },
                                                        onPlayMovie = { viewModel.playMovie(it) },
                                                        onAddMovieCategory = { viewModel.addMovieCategory(it) },
                                                        onDeleteMovieCategory = { viewModel.deleteMovieCategory(it) },
                                                        onAddOrUpdateSeries = { viewModel.saveSeries(it) },
                                                        onDeleteSeries = { viewModel.deleteSeries(it) },
                                                        onToggleSeriesPublish = { viewModel.toggleSeriesPublish(it) },
                                                        onAddOrUpdateEpisode = { viewModel.saveEpisode(it) },
                                                        onDeleteEpisode = { viewModel.repository.deleteEpisode(it, user.uid) },
                                                        onToggleEpisodePublish = { viewModel.toggleEpisodePublish(it) },
                                                        onToggleEpisodeEnabled = { viewModel.toggleEpisodeEnabled(it) },
                                                        onPlayEpisode = { viewModel.playEpisode(it) },
                                                        onSyncPlaylist = { viewModel.refreshPlaylist(it) },
                                                        onTestConnectivity = { viewModel.checkChannelConnectivity(it) },
                                                        onToggleUserStatus = { viewModel.toggleUserStatus(it) },
                                                        onToggleUserRole = { viewModel.toggleUserRole(it) },
                                                        onDeleteUser = { viewModel.deleteUser(it) },
                                                        adConfig = adConfig,
                                                        isAdSaving = uiState.isAdSaving,
                                                        onSaveAdConfig = { html, enabled, top, bottom -> viewModel.saveAdConfig(html, enabled, top, bottom) },
                                                        onToggleAdEnabled = { viewModel.toggleAdEnabled() },
                                                        onToggleAdTopEnabled = { viewModel.toggleAdTopEnabled() },
                                                        onToggleAdBottomEnabled = { viewModel.toggleAdBottomEnabled() },
                                                        onClearAdConfig = { viewModel.clearAdConfig() },
                                                        isSyncing = uiState.isRefreshingPlaylist,
                                                        syncStatus = uiState.syncNotification,
                                                        plans = plans,
                                                        manualUpiConfig = manualUpiConfig,
                                                        manualPaymentRequests = manualPaymentRequests,
                                                        isMasterPremiumEnabled = isMasterPremiumEnabled,
                                                        onSaveManualUpiConfig = { viewModel.saveManualUpiConfig(it) },
                                                        onAcceptManualPayment = { reqId -> viewModel.acceptManualPayment(reqId) },
                                                        onRejectManualPayment = { reqId, reason -> viewModel.rejectManualPayment(reqId, reason) },
                                                        onGrantUserPremium = { viewModel.grantUserPremium(it) },
                                                        onRevokeUserPremium = { viewModel.revokeUserPremium(it) },
                                                        onToggleMasterPremium = { viewModel.toggleMasterPremium() },
                                                        onSavePlan = { viewModel.savePlan(it) },
                                                        onDeletePlan = { viewModel.deletePlan(it) },
                                                        onLogout = { viewModel.logoutAdmin() }
                                                    )

                                                    else -> {}
                                                }
                                            }
                                        }

                                        // Placement B — Footer Advertisement (Positioned immediately above Footer)
                                        if (!uiState.isFullScreen && uiState.currentScreen != Screen.ADMIN && adConfig.isBottomVisible) {
                                            AdBannerView(
                                                adConfig = adConfig,
                                                placementTag = "footer_ad_banner",
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    // Dialogs & Modals
                    if (uiState.selectedMovie != null) {
                        MovieDetailSheet(
                            movie = uiState.selectedMovie!!,
                            isInWatchlist = watchlist.any { it.contentId == uiState.selectedMovie!!.id },
                            onDismiss = { viewModel.selectMovie(null) },
                            onPlay = { viewModel.playMovie(uiState.selectedMovie!!) },
                            onToggleWatchlist = { viewModel.toggleWatchlistMovie(uiState.selectedMovie!!) }
                        )
                    }

                    if (uiState.showEditChannelModal) {
                        EditChannelDialog(
                            channelToEdit = uiState.channelToEdit,
                            onDismiss = { viewModel.dismissEditChannelModal() },
                            onSave = { viewModel.saveChannel(it) }
                        )
                    }

                    if (uiState.showForgotPasswordDialog) {
                        ForgotPasswordDialog(
                            isLoading = uiState.isAuthLoading,
                            successMessage = uiState.resetPasswordMessage,
                            errorMessage = uiState.authError,
                            onDismiss = { viewModel.dismissForgotPassword() },
                            onSubmit = { email -> viewModel.sendPasswordReset(email) }
                        )
                    }

                    if (uiState.selectedPlanForCheckout != null) {
                        ManualUpiCheckoutDialog(
                            plan = uiState.selectedPlanForCheckout!!,
                            config = manualUpiConfig,
                            isSubmitting = uiState.isAuthLoading,
                            submissionMessage = uiState.manualPaymentSubmissionMessage,
                            activeRequest = manualPaymentRequests.find { it.userId == user.uid && it.planId == uiState.selectedPlanForCheckout!!.id && (it.status == "PENDING" || it.status == "ACCEPTED") },
                            onDismiss = { viewModel.dismissCheckout() },
                            onSubmitPayment = { utr, screenshot ->
                                viewModel.submitManualPayment(
                                    uiState.selectedPlanForCheckout!!,
                                    utr,
                                    screenshot
                                )
                            }
                        )
                    }

                    if (uiState.premiumAlertMovie != null) {
                        PremiumLockAlert(
                            movie = uiState.premiumAlertMovie!!,
                            onDismiss = { viewModel.dismissPremiumAlert() },
                            onUpgrade = {
                                viewModel.navigateTo(Screen.SUBSCRIPTIONS)
                                viewModel.dismissPremiumAlert()
                            }
                        )
                    }

                    if (uiState.showPaymentSuccessDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.dismissPaymentSuccess() },
                            title = { Text("Subscription Activated!", color = Amber500) },
                            text = { Text("Welcome to TamilStream VIP! You now have unlimited access to live channels and 4K blockbuster movies.") },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.dismissPaymentSuccess() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack)
                                ) {
                                    Text("Start Watching")
                                }
                            },
                            containerColor = DarkSurface
                        )
                    }
                }
            }
        }
    }
}
