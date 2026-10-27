package com.example.tamilstream.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamilstream.data.model.*
import com.example.tamilstream.data.repository.M3uPlaylistService
import com.example.tamilstream.data.repository.TamilStreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    LOGIN,
    REGISTER,
    ADMIN_LOGIN,
    HOME,
    LIVE_TV,
    MOVIES,
    SEARCH,
    SUBSCRIPTIONS,
    WATCHLIST,
    EPISODES,
    PROFILE,
    ADMIN
}

data class UiState(
    val currentScreen: Screen = Screen.SPLASH,
    val activePlayback: PlaybackItem? = null,
    val isPlaying: Boolean = true,
    val isMuted: Boolean = false,
    val isFullScreen: Boolean = false,
    val activeCategory: String = "All",
    val channelSearchQuery: String = "",
    val channelViewMode: String = "grid", // "grid" or "list"
    val showOnlyFavorites: Boolean = false,
    val selectedMovie: Movie? = null,
    val selectedPlanForCheckout: SubscriptionPlan? = null,
    val showPaymentSuccessDialog: Boolean = false,
    val showAdminLoginModal: Boolean = false,
    val isAdminAuthenticated: Boolean = false,
    val channelToEdit: Channel? = null,
    val showEditChannelModal: Boolean = false,
    val searchQuery: String = "",
    val premiumAlertMovie: Movie? = null,
    val isRefreshingPlaylist: Boolean = false,
    val syncNotification: String? = null,
    val playbackError: String? = null,
    // Auth & User Management State
    val authError: String? = null,
    val isAuthLoading: Boolean = false,
    val resetPasswordMessage: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val userSearchQuery: String = "",
    val registeredUsers: List<User> = emptyList(),
    val isGuestMode: Boolean = false,
    val authPromptReason: String? = null,
    val pendingPlayback: PlaybackItem? = null,
    val authReturnScreen: Screen = Screen.HOME,
    // Episodes State
    val episodeSearchQuery: String = "",
    val episodeStatusFilter: String = "All", // "All", "Published", "Draft", "Unpublished"
    val selectedEpisodeForEdit: Episode? = null,
    val showEditEpisodeModal: Boolean = false,
    val episodeToDelete: Episode? = null,
    val showDeleteEpisodeDialog: Boolean = false,
    val previewingEpisode: Episode? = null,
    // Advertisement System State
    val isAdSaving: Boolean = false,
    // Manual UPI Payment & Premium Management State
    val activeManualPaymentRequest: ManualPaymentRequest? = null,
    val isManualPaymentSubmitting: Boolean = false,
    val manualPaymentSubmissionMessage: String? = null,
    val editingPlan: SubscriptionPlan? = null,
    val showPlanEditDialog: Boolean = false
)

class TamilStreamViewModel(
    val repository: TamilStreamRepository = TamilStreamRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val channels = repository.channels
    val movies = repository.movies
    val movieCategories = repository.movieCategories
    val seriesList = repository.seriesList
    val episodes = repository.episodes
    val plans = repository.plans
    val watchlist = repository.watchlist
    val watchHistory = repository.watchHistory
    val currentUser = repository.currentUser
    val currentSubscription = repository.currentSubscription
    val lastPlaybackDiagnostic = repository.lastPlaybackDiagnostic
    val adConfig = repository.adConfig
    val manualUpiConfig = repository.manualUpiConfig
    val manualPaymentRequests = repository.manualPaymentRequests
    val isMasterPremiumEnabled = repository.isMasterPremiumEnabled

    fun saveAdConfig(
        htmlContent: String,
        enabled: Boolean,
        topEnabled: Boolean = true,
        bottomEnabled: Boolean = true,
        onComplete: ((Result<AdConfig>) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAdSaving = true) }
            try {
                val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
                val result = repository.saveAdConfig(htmlContent, enabled, topEnabled, bottomEnabled, adminUid)
                onComplete?.invoke(result)
            } catch (e: Exception) {
                onComplete?.invoke(Result.failure(e))
            } finally {
                _uiState.update { it.copy(isAdSaving = false) }
            }
        }
    }

    fun toggleAdEnabled(): Result<AdConfig> {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        return repository.toggleAdEnabled(adminUid)
    }

    fun toggleAdTopEnabled(): Result<AdConfig> {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        return repository.toggleAdTopEnabled(adminUid)
    }

    fun toggleAdBottomEnabled(): Result<AdConfig> {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        return repository.toggleAdBottomEnabled(adminUid)
    }

    fun clearAdConfig(): Result<AdConfig> {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        return repository.clearAdConfig(adminUid)
    }

    init {
        // Automatically sync live channels from iptv-org playlist on startup
        refreshPlaylist()
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun requestAuthForPlayback(playback: PlaybackItem, reason: String) {
        _uiState.update {
            it.copy(
                pendingPlayback = playback,
                authPromptReason = reason,
                authReturnScreen = it.currentScreen,
                currentScreen = Screen.LOGIN,
                authError = null
            )
        }
    }

    fun requestAuthForFeature(reason: String) {
        _uiState.update {
            it.copy(
                pendingPlayback = null,
                authPromptReason = reason,
                authReturnScreen = it.currentScreen,
                currentScreen = Screen.LOGIN,
                authError = null
            )
        }
    }

    fun cancelAuth() {
        val returnScreen = _uiState.value.authReturnScreen
        val targetScreen = if (returnScreen == Screen.LOGIN || returnScreen == Screen.REGISTER) Screen.HOME else returnScreen
        _uiState.update {
            it.copy(
                currentScreen = targetScreen,
                pendingPlayback = null,
                authPromptReason = null,
                authError = null
            )
        }
    }

    fun playChannel(channel: Channel) {
        val playback = PlaybackItem(
            id = channel.id,
            title = channel.cleanName,
            streamUrl = channel.url,
            fallbackUrl = channel.fallbackUrl,
            thumbnail = channel.logo,
            category = channel.group,
            isLive = true
        )

        // Require authentication for video stream playback
        if (!currentUser.value.isAuthenticated) {
            requestAuthForPlayback(
                playback = playback,
                reason = "Please sign in or create an account to watch \"${channel.cleanName}\" live stream."
            )
            return
        }

        _uiState.update {
            it.copy(
                activePlayback = playback,
                isPlaying = true
            )
        }
        repository.recordHistory(
            WatchHistoryItem(
                id = "hist_${System.currentTimeMillis()}",
                contentId = channel.id,
                contentType = "channel",
                title = channel.cleanName,
                thumbnail = channel.logo,
                durationSeconds = 0
            )
        )
    }

    fun playMovie(movie: Movie) {
        val playback = PlaybackItem(
            id = movie.id,
            title = movie.title,
            streamUrl = movie.videoUrl,
            thumbnail = movie.thumbnail,
            category = movie.genre.firstOrNull() ?: "Movie",
            isLive = false,
            durationMinutes = movie.duration
        )

        // Require authentication for video stream playback
        if (!currentUser.value.isAuthenticated) {
            requestAuthForPlayback(
                playback = playback,
                reason = "Please sign in or create an account to watch \"${movie.title}\"."
            )
            return
        }

        // If movie is premium and user is not subscribed, prompt upgrade
        if (movie.isPremium && !currentUser.value.isPremium) {
            _uiState.update { it.copy(premiumAlertMovie = movie) }
            return
        }

        _uiState.update {
            it.copy(
                activePlayback = playback,
                isPlaying = true,
                selectedMovie = null
            )
        }
        repository.recordHistory(
            WatchHistoryItem(
                id = "hist_${System.currentTimeMillis()}",
                contentId = movie.id,
                contentType = "movie",
                title = movie.title,
                thumbnail = movie.thumbnail,
                durationSeconds = (movie.duration * 60).toLong()
            )
        )
    }

    fun addMovie(movie: Movie) {
        saveMovie(movie)
    }

    fun saveMovie(movie: Movie) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.saveMovie(movie, adminUid)
    }

    fun deleteMovie(movieId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.deleteMovie(movieId, adminUid)
    }

    fun toggleMoviePublish(movieId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.toggleMoviePublish(movieId, adminUid)
    }

    fun toggleMovieFeatured(movieId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.toggleMovieFeatured(movieId, adminUid)
    }

    fun addMovieCategory(category: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.addMovieCategory(category, adminUid)
    }

    fun deleteMovieCategory(category: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.deleteMovieCategory(category, adminUid)
    }

    fun playEpisode(episode: Episode) {
        val playback = PlaybackItem(
            id = episode.id,
            title = episode.title,
            streamUrl = episode.videoUrl,
            thumbnail = episode.thumbnailUrl,
            category = episode.seriesName,
            isLive = false,
            durationMinutes = episode.durationMinutes
        )

        // Require authentication for video stream playback
        if (!currentUser.value.isAuthenticated) {
            requestAuthForPlayback(
                playback = playback,
                reason = "Please sign in or create an account to watch \"${episode.title}\"."
            )
            return
        }

        _uiState.update {
            it.copy(
                activePlayback = playback,
                isPlaying = true
            )
        }
        repository.recordHistory(
            WatchHistoryItem(
                id = "hist_${System.currentTimeMillis()}",
                contentId = episode.id,
                contentType = "episode",
                title = episode.title,
                thumbnail = episode.thumbnailUrl,
                durationSeconds = (episode.durationMinutes * 60).toLong()
            )
        )
    }

    fun openAddEpisodeModal() {
        _uiState.update {
            it.copy(
                selectedEpisodeForEdit = null,
                showEditEpisodeModal = true
            )
        }
    }

    fun openEditEpisodeModal(episode: Episode) {
        _uiState.update {
            it.copy(
                selectedEpisodeForEdit = episode,
                showEditEpisodeModal = true
            )
        }
    }

    fun dismissEditEpisodeModal() {
        _uiState.update {
            it.copy(
                selectedEpisodeForEdit = null,
                showEditEpisodeModal = false
            )
        }
    }

    fun saveSeries(series: Series) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.saveSeries(series, adminUid)
    }

    fun deleteSeries(seriesId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.deleteSeries(seriesId, adminUid)
    }

    fun toggleSeriesPublish(seriesId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.toggleSeriesPublish(seriesId, adminUid)
    }

    fun saveEpisode(episode: Episode) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.saveEpisode(episode, adminUid)
        dismissEditEpisodeModal()
    }

    fun promptDeleteEpisode(episode: Episode) {
        _uiState.update {
            it.copy(
                episodeToDelete = episode,
                showDeleteEpisodeDialog = true
            )
        }
    }

    fun dismissDeleteEpisodeDialog() {
        _uiState.update {
            it.copy(
                episodeToDelete = null,
                showDeleteEpisodeDialog = false
            )
        }
    }

    fun confirmDeleteEpisode() {
        val ep = _uiState.value.episodeToDelete
        if (ep != null) {
            val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
            repository.deleteEpisode(ep.id, adminUid)
        }
        dismissDeleteEpisodeDialog()
    }

    fun toggleEpisodePublish(episodeId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.toggleEpisodePublish(episodeId, adminUid)
    }

    fun toggleEpisodeEnabled(episodeId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.toggleEpisodeEnabled(episodeId, adminUid)
    }

    fun setEpisodeSearchQuery(query: String) {
        _uiState.update { it.copy(episodeSearchQuery = query) }
    }

    fun setEpisodeStatusFilter(filter: String) {
        _uiState.update { it.copy(episodeStatusFilter = filter) }
    }

    fun dismissPremiumAlert() {
        _uiState.update { it.copy(premiumAlertMovie = null) }
    }

    fun stopPlayback() {
        _uiState.update { it.copy(activePlayback = null, isPlaying = false, isFullScreen = false) }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleFullScreen() {
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    fun setActiveCategory(category: String) {
        _uiState.update { it.copy(activeCategory = category) }
    }

    fun setChannelSearch(query: String) {
        _uiState.update { it.copy(channelSearchQuery = query) }
    }

    fun setUniversalSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(channelViewMode = if (it.channelViewMode == "grid") "list" else "grid")
        }
    }

    fun toggleFavoritesOnly() {
        _uiState.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites) }
    }

    fun toggleFavorite(channelId: String) {
        repository.toggleFavorite(channelId)
    }

    fun selectMovie(movie: Movie?) {
        _uiState.update { it.copy(selectedMovie = movie) }
    }

    fun toggleWatchlistMovie(movie: Movie) {
        repository.toggleWatchlist(
            WatchlistItem(
                id = "watch_${movie.id}",
                contentId = movie.id,
                contentType = "movie",
                title = movie.title,
                poster = movie.poster,
                rating = movie.rating,
                releaseYear = movie.releaseYear,
                category = movie.genre.firstOrNull()
            )
        )
    }

    fun openCheckout(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlanForCheckout = plan, premiumAlertMovie = null) }
    }

    fun cancelCheckout() {
        _uiState.update { it.copy(selectedPlanForCheckout = null) }
    }

    fun completeCheckout(plan: SubscriptionPlan) {
        repository.activateSubscription(plan)
        _uiState.update {
            it.copy(
                selectedPlanForCheckout = null,
                showPaymentSuccessDialog = true
            )
        }
    }

    fun dismissPaymentSuccess() {
        _uiState.update { it.copy(showPaymentSuccessDialog = false) }
    }

    fun openAdminLogin() {
        _uiState.update { it.copy(currentScreen = Screen.ADMIN_LOGIN, authError = null) }
    }

    fun dismissAdminLogin() {
        _uiState.update { it.copy(currentScreen = Screen.HOME, authError = null) }
    }

    fun checkInitialSession() {
        val user = repository.currentUser.value
        if (user.id != "user_guest" && user.isActive) {
            _uiState.update { it.copy(currentScreen = Screen.HOME) }
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null, resetPasswordMessage = null) }
    }

    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        pass: String,
        confirm: String,
        terms: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            val res = repository.registerUser(fullName, username, email, pass, confirm, terms)
            _uiState.update { it.copy(isAuthLoading = false) }
            when (res) {
                is AuthResult.Success -> {
                    refreshRegisteredUsers()
                    val pending = _uiState.value.pendingPlayback
                    val returnScreen = _uiState.value.authReturnScreen
                    val targetScreen = if (returnScreen == Screen.LOGIN || returnScreen == Screen.REGISTER) Screen.HOME else returnScreen
                    _uiState.update {
                        it.copy(
                            currentScreen = targetScreen,
                            isGuestMode = false,
                            authError = null,
                            authPromptReason = null,
                            pendingPlayback = null,
                            activePlayback = pending ?: it.activePlayback,
                            isPlaying = if (pending != null) true else it.isPlaying
                        )
                    }
                    if (pending != null) {
                        repository.recordHistory(
                            WatchHistoryItem(
                                id = "hist_${System.currentTimeMillis()}",
                                contentId = pending.id,
                                contentType = if (pending.isLive) "channel" else "video",
                                title = pending.title,
                                thumbnail = pending.thumbnail,
                                durationSeconds = (pending.durationMinutes * 60).toLong()
                            )
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(authError = res.message) }
                }
            }
        }
    }

    fun loginUser(
        identifier: String,
        pass: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            val res = repository.loginUser(identifier, pass, rememberMe)
            _uiState.update { it.copy(isAuthLoading = false) }
            when (res) {
                is AuthResult.Success -> {
                    val pending = _uiState.value.pendingPlayback
                    val returnScreen = _uiState.value.authReturnScreen
                    val targetScreen = if (returnScreen == Screen.LOGIN || returnScreen == Screen.REGISTER) Screen.HOME else returnScreen
                    _uiState.update {
                        it.copy(
                            currentScreen = targetScreen,
                            isGuestMode = false,
                            authError = null,
                            authPromptReason = null,
                            pendingPlayback = null,
                            activePlayback = pending ?: it.activePlayback,
                            isPlaying = if (pending != null) true else it.isPlaying
                        )
                    }
                    if (pending != null) {
                        repository.recordHistory(
                            WatchHistoryItem(
                                id = "hist_${System.currentTimeMillis()}",
                                contentId = pending.id,
                                contentType = if (pending.isLive) "channel" else "video",
                                title = pending.title,
                                thumbnail = pending.thumbnail,
                                durationSeconds = (pending.durationMinutes * 60).toLong()
                            )
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(authError = res.message) }
                }
            }
        }
    }

    fun loginAdmin(
        identifier: String,
        pass: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            val res = repository.loginAdmin(identifier, pass, rememberMe)
            _uiState.update { it.copy(isAuthLoading = false) }
            when (res) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAdminAuthenticated = true,
                            currentScreen = Screen.ADMIN,
                            authError = null
                        )
                    }
                    refreshRegisteredUsers()
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(authError = res.message) }
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null, resetPasswordMessage = null) }
            val res = repository.sendPasswordReset(email)
            _uiState.update { it.copy(isAuthLoading = false) }
            res.fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(resetPasswordMessage = msg, authError = null) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(authError = err.message ?: "Failed to dispatch reset email.") }
                }
            )
        }
    }

    fun openForgotPassword() {
        _uiState.update { it.copy(showForgotPasswordDialog = true, resetPasswordMessage = null, authError = null) }
    }

    fun dismissForgotPassword() {
        _uiState.update { it.copy(showForgotPasswordDialog = false, resetPasswordMessage = null, authError = null) }
    }

    fun continueAsGuest() {
        repository.continueAsGuest()
        _uiState.update {
            it.copy(
                currentScreen = Screen.HOME,
                isGuestMode = true,
                authError = null,
                authPromptReason = null,
                pendingPlayback = null
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update {
            it.copy(
                currentScreen = Screen.HOME,
                isAdminAuthenticated = false,
                isGuestMode = true,
                activePlayback = null,
                isPlaying = false,
                isFullScreen = false,
                pendingPlayback = null,
                authPromptReason = null,
                authError = null
            )
        }
    }

    fun logoutAdmin() {
        _uiState.update {
            it.copy(
                isAdminAuthenticated = false,
                currentScreen = Screen.HOME
            )
        }
    }

    fun updateProfile(displayName: String, avatar: String) {
        viewModelScope.launch {
            val uid = currentUser.value.uid
            repository.updateProfile(uid, displayName, avatar)
        }
    }

    fun refreshRegisteredUsers() {
        val users = repository.registeredUsers
        _uiState.update { it.copy(registeredUsers = users) }
    }

    fun setUserSearch(query: String) {
        _uiState.update { it.copy(userSearchQuery = query) }
    }

    fun toggleUserStatus(uid: String) {
        viewModelScope.launch {
            val adminUid = currentUser.value.uid
            val res = repository.toggleUserStatus(uid, adminUid)
            if (res.isSuccess) {
                refreshRegisteredUsers()
            } else {
                _uiState.update { it.copy(authError = res.exceptionOrNull()?.message) }
            }
        }
    }

    fun toggleUserRole(uid: String) {
        viewModelScope.launch {
            val adminUid = currentUser.value.uid
            val res = repository.toggleUserRole(uid, adminUid)
            if (res.isSuccess) {
                refreshRegisteredUsers()
            } else {
                _uiState.update { it.copy(authError = res.exceptionOrNull()?.message) }
            }
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            val adminUid = currentUser.value.uid
            val res = repository.deleteUser(uid, adminUid)
            if (res.isSuccess) {
                refreshRegisteredUsers()
            } else {
                _uiState.update { it.copy(authError = res.exceptionOrNull()?.message) }
            }
        }
    }

    fun openEditChannelModal(channel: Channel?) {
        _uiState.update {
            it.copy(
                channelToEdit = channel,
                showEditChannelModal = true
            )
        }
    }

    fun dismissEditChannelModal() {
        _uiState.update {
            it.copy(
                channelToEdit = null,
                showEditChannelModal = false
            )
        }
    }

    fun saveChannel(channel: Channel) {
        if (channels.value.any { it.id == channel.id }) {
            repository.updateChannel(channel)
        } else {
            repository.addChannel(channel)
        }
        dismissEditChannelModal()
    }

    fun deleteChannel(channelId: String) {
        repository.deleteChannel(channelId)
    }

    fun clearHistory() {
        repository.clearHistory()
    }

    fun recordDiagnostic(diag: PlaybackDiagnostic) {
        repository.recordPlaybackDiagnostic(diag)
    }

    fun checkChannelConnectivity(channelId: String) {
        viewModelScope.launch {
            repository.checkChannelConnectivity(channelId)
        }
    }

    fun refreshPlaylist(url: String = M3uPlaylistService.DEFAULT_M3U_URL) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingPlaylist = true, syncNotification = "Syncing M3U playlist...") }
            val result = repository.syncWithM3u(url)
            _uiState.update {
                it.copy(
                    isRefreshingPlaylist = false,
                    syncNotification = if (result.validCount > 0) {
                        "Successfully synced ${result.validCount} live Tamil channels!"
                    } else {
                        result.errorMessage ?: "Playlist updated"
                    }
                )
            }
        }
    }

    fun dismissSyncNotification() {
        _uiState.update { it.copy(syncNotification = null) }
    }

    fun playNextChannel() {
        val current = _uiState.value.activePlayback ?: return
        val next = repository.getNextChannel(current.id)
        if (next != null) {
            playChannel(next)
        }
    }

    fun playPreviousChannel() {
        val current = _uiState.value.activePlayback ?: return
        val prev = repository.getPreviousChannel(current.id)
        if (prev != null) {
            playChannel(prev)
        }
    }

    fun retryPlayback() {
        val current = _uiState.value.activePlayback ?: return
        _uiState.update { it.copy(isPlaying = false, playbackError = null) }
        _uiState.update { it.copy(isPlaying = true) }
    }

    // Manual UPI Payment & Premium Management ViewModel Functions

    fun selectPlanForCheckout(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlanForCheckout = plan, manualPaymentSubmissionMessage = null) }
    }

    fun dismissCheckout() {
        _uiState.update { it.copy(selectedPlanForCheckout = null, manualPaymentSubmissionMessage = null, activeManualPaymentRequest = null) }
    }

    fun submitManualPayment(plan: SubscriptionPlan, paymentIdentifier: String, screenshotUrl: String) {
        val user = currentUser.value
        viewModelScope.launch {
            _uiState.update { it.copy(isManualPaymentSubmitting = true, manualPaymentSubmissionMessage = null) }
            val req = repository.submitManualPayment(plan, paymentIdentifier, screenshotUrl, user)
            _uiState.update {
                it.copy(
                    isManualPaymentSubmitting = false,
                    activeManualPaymentRequest = req,
                    manualPaymentSubmissionMessage = "Payment submitted successfully. Your payment is awaiting admin verification."
                )
            }
        }
    }

    fun acceptManualPayment(requestId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.acceptManualPayment(requestId, adminUid)
    }

    fun rejectManualPayment(requestId: String, reason: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.rejectManualPayment(requestId, reason, adminUid)
    }

    fun saveManualUpiConfig(config: ManualUpiConfig) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.saveManualUpiConfig(config, adminUid)
    }

    fun grantUserPremium(uid: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.grantUserPremium(uid, adminUid)
        _uiState.update { it.copy(registeredUsers = repository.registeredUsers) }
    }

    fun revokeUserPremium(uid: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.revokeUserPremium(uid, adminUid)
        _uiState.update { it.copy(registeredUsers = repository.registeredUsers) }
    }

    fun toggleMasterPremium() {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.toggleMasterPremium(adminUid)
    }

    fun savePlan(plan: SubscriptionPlan) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.saveSubscriptionPlan(plan, adminUid)
        _uiState.update { it.copy(showPlanEditDialog = false, editingPlan = null) }
    }

    fun deletePlan(planId: String) {
        val adminUid = currentUser.value.uid.ifBlank { "admin_root_master" }
        repository.deleteSubscriptionPlan(planId, adminUid)
    }

    fun openPlanEditor(plan: SubscriptionPlan?) {
        _uiState.update { it.copy(editingPlan = plan, showPlanEditDialog = true) }
    }

    fun closePlanEditor() {
        _uiState.update { it.copy(editingPlan = null, showPlanEditDialog = false) }
    }
}

