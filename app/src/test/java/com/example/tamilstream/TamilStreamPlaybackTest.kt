package com.example.tamilstream

import com.example.tamilstream.data.model.PlaybackItem
import com.example.tamilstream.ui.viewmodel.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TamilStreamPlaybackTest {

    @Test
    fun testFullscreenToggleAndPlaybackState() {
        var state = UiState()
        assertFalse("Initial state should not be fullscreen", state.isFullScreen)

        state = state.copy(isFullScreen = !state.isFullScreen)
        assertTrue("After toggle, state must be fullscreen", state.isFullScreen)

        state = state.copy(isFullScreen = !state.isFullScreen)
        assertFalse("After second toggle, state must exit fullscreen", state.isFullScreen)
    }

    @Test
    fun testActivePlaybackItemProperties() {
        val playback = PlaybackItem(
            id = "sun_tv",
            title = "Sun TV Tamil HD",
            streamUrl = "https://example.com/live/suntv.m3u8",
            thumbnail = "https://example.com/logo.png",
            category = "Entertainment",
            isLive = true
        )

        var state = UiState(activePlayback = playback, isPlaying = true)
        assertNotNull("Active playback item must not be null", state.activePlayback)
        assertEquals("sun_tv", state.activePlayback?.id)
        assertEquals("Sun TV Tamil HD", state.activePlayback?.title)
        assertTrue(state.isPlaying)

        // Enter fullscreen
        state = state.copy(isFullScreen = true)
        assertTrue(state.isFullScreen)

        // Stopping playback resets activePlayback and exits fullscreen
        state = state.copy(activePlayback = null, isPlaying = false, isFullScreen = false)
        assertNull("Active playback must be null after stop", state.activePlayback)
        assertFalse("Fullscreen must be reset when playback stops", state.isFullScreen)
        assertFalse("Playing state must be false after stop", state.isPlaying)
    }

    @Test
    fun testGuestUserModelAndAuthenticationCheck() {
        val guestUser = com.example.tamilstream.data.model.User(
            id = "user_guest",
            uid = "user_guest",
            username = "guest",
            email = "guest@tamilstream.live",
            displayName = "Guest Viewer"
        )
        assertTrue("Guest user id must be identified as guest", guestUser.isGuest)
        assertFalse("Guest user must not be marked authenticated", guestUser.isAuthenticated)

        val authenticatedUser = com.example.tamilstream.data.model.User(
            id = "usr_real_123",
            uid = "usr_real_123",
            username = "sanjeevi",
            email = "user@example.com",
            displayName = "Sanjeevi",
            accountStatus = "active"
        )
        assertFalse("Real registered user is not guest", authenticatedUser.isGuest)
        assertTrue("Active registered user must be authenticated", authenticatedUser.isAuthenticated)
    }

    @Test
    fun testGuestPlaybackPromptAndCancellation() {
        val channelPlayback = PlaybackItem(
            id = "sun_tv_live",
            title = "Sun TV",
            streamUrl = "https://example.com/stream.m3u8",
            thumbnail = "https://example.com/logo.png",
            category = "General",
            isLive = true
        )

        // Starting in HOME screen as a guest
        var state = UiState(currentScreen = com.example.tamilstream.ui.viewmodel.Screen.HOME)

        // Simulating guest intercept: request auth for playback
        val reason = "Please sign in to watch Sun TV"
        state = state.copy(
            pendingPlayback = channelPlayback,
            authPromptReason = reason,
            authReturnScreen = state.currentScreen,
            currentScreen = com.example.tamilstream.ui.viewmodel.Screen.LOGIN
        )

        assertEquals(com.example.tamilstream.ui.viewmodel.Screen.LOGIN, state.currentScreen)
        assertEquals(com.example.tamilstream.ui.viewmodel.Screen.HOME, state.authReturnScreen)
        assertEquals(channelPlayback, state.pendingPlayback)
        assertEquals(reason, state.authPromptReason)
        assertNull("Playback must NOT start before authentication", state.activePlayback)

        // User cancels / returns to browsing
        val returnScreen = state.authReturnScreen
        state = state.copy(
            currentScreen = returnScreen,
            pendingPlayback = null,
            authPromptReason = null
        )

        assertEquals(com.example.tamilstream.ui.viewmodel.Screen.HOME, state.currentScreen)
        assertNull(state.pendingPlayback)
        assertNull(state.authPromptReason)
        assertNull(state.activePlayback)
    }

    @Test
    fun testPlaybackAutoResumeOnSuccessfulLogin() {
        val channelPlayback = PlaybackItem(
            id = "ch_vijay_tv",
            title = "Vijay TV HD",
            streamUrl = "https://example.com/vijay.m3u8",
            thumbnail = "https://example.com/vijay.png",
            category = "Entertainment",
            isLive = true
        )

        // User attempted playback and was directed to LOGIN
        var state = UiState(
            currentScreen = com.example.tamilstream.ui.viewmodel.Screen.LOGIN,
            authReturnScreen = com.example.tamilstream.ui.viewmodel.Screen.HOME,
            pendingPlayback = channelPlayback,
            authPromptReason = "Sign in to watch Vijay TV HD"
        )

        // Simulate successful login
        val pending = state.pendingPlayback
        val targetScreen = state.authReturnScreen
        state = state.copy(
            currentScreen = targetScreen,
            isGuestMode = false,
            pendingPlayback = null,
            authPromptReason = null,
            activePlayback = pending,
            isPlaying = pending != null
        )

        assertEquals(com.example.tamilstream.ui.viewmodel.Screen.HOME, state.currentScreen)
        assertNull("Pending playback should be cleared after auto-resume", state.pendingPlayback)
        assertNotNull("Active playback must now be running", state.activePlayback)
        assertEquals("ch_vijay_tv", state.activePlayback?.id)
        assertTrue("Playback must automatically play", state.isPlaying)
    }
}
