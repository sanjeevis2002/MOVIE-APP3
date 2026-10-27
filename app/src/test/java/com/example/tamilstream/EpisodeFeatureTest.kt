package com.example.tamilstream

import com.example.tamilstream.data.model.Episode
import com.example.tamilstream.data.model.PlaybackItem
import com.example.tamilstream.data.repository.TamilStreamRepository
import com.example.tamilstream.ui.viewmodel.Screen
import com.example.tamilstream.ui.viewmodel.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeFeatureTest {

    @Test
    fun testEpisodeVisibilityLogic() {
        val publishedEnabled = Episode(
            title = "Test Ep 1",
            thumbnailUrl = "https://example.com/thumb.jpg",
            videoUrl = "https://example.com/stream.m3u8",
            status = "Published",
            isEnabled = true
        )
        assertTrue("Published and enabled episode must be visible to normal users", publishedEnabled.isVisibleToUsers)

        val draftEpisode = Episode(
            title = "Draft Ep",
            thumbnailUrl = "https://example.com/thumb.jpg",
            videoUrl = "https://example.com/stream.m3u8",
            status = "Draft",
            isEnabled = true
        )
        assertFalse("Draft episode must NOT be visible to normal users", draftEpisode.isVisibleToUsers)

        val disabledEpisode = Episode(
            title = "Disabled Ep",
            thumbnailUrl = "https://example.com/thumb.jpg",
            videoUrl = "https://example.com/stream.m3u8",
            status = "Published",
            isEnabled = false
        )
        assertFalse("Disabled episode must NOT be visible to normal users", disabledEpisode.isVisibleToUsers)

        val unpublishedEpisode = Episode(
            title = "Unpublished Ep",
            thumbnailUrl = "https://example.com/thumb.jpg",
            videoUrl = "https://example.com/stream.m3u8",
            status = "Unpublished",
            isEnabled = true
        )
        assertFalse("Unpublished episode must NOT be visible to normal users", unpublishedEpisode.isVisibleToUsers)
    }

    @Test
    fun testEpisodeCrudOperations() {
        val repository = TamilStreamRepository()
        val initialCount = repository.episodes.value.size
        assertTrue("Repository must provide initial seed episodes", initialCount > 0)

        // Add
        val newEpisode = Episode(
            id = "ep_test_custom",
            title = "Custom Special Episode",
            seriesName = "Custom Serials",
            thumbnailUrl = "https://example.com/thumb.jpg",
            videoUrl = "https://example.com/custom.m3u8",
            description = "A special celebration episode",
            status = "Published",
            isFeatured = true,
            isEnabled = true
        )
        repository.addEpisode(newEpisode)
        assertEquals(initialCount + 1, repository.episodes.value.size)
        assertTrue(repository.episodes.value.any { it.id == "ep_test_custom" })

        // Update
        val updated = newEpisode.copy(title = "Custom Special Episode (Remastered)")
        repository.updateEpisode(updated)
        val fetched = repository.episodes.value.find { it.id == "ep_test_custom" }
        assertNotNull(fetched)
        assertEquals("Custom Special Episode (Remastered)", fetched?.title)

        // Toggle Publish
        repository.toggleEpisodePublish("ep_test_custom")
        val toggledStatus = repository.episodes.value.find { it.id == "ep_test_custom" }?.status
        assertEquals("Unpublished", toggledStatus)

        // Toggle Enabled
        repository.toggleEpisodeEnabled("ep_test_custom")
        val toggledEnabled = repository.episodes.value.find { it.id == "ep_test_custom" }?.isEnabled
        assertEquals(false, toggledEnabled)

        // Delete
        repository.deleteEpisode("ep_test_custom")
        assertEquals(initialCount, repository.episodes.value.size)
        assertFalse(repository.episodes.value.any { it.id == "ep_test_custom" })
    }

    @Test
    fun testEpisodePlaybackStateTransitions() {
        val sampleEpisode = Episode(
            id = "ep_play_test",
            title = "Test Streaming Episode",
            seriesName = "Tamil Thriller",
            thumbnailUrl = "https://example.com/thumb.jpg",
            videoUrl = "https://example.com/test_stream.m3u8",
            durationMinutes = 26
        )

        var uiState = UiState()
        val playback = com.example.tamilstream.data.model.PlaybackItem(
            id = sampleEpisode.id,
            title = sampleEpisode.title,
            streamUrl = sampleEpisode.videoUrl,
            thumbnail = sampleEpisode.thumbnailUrl,
            category = sampleEpisode.seriesName,
            isLive = false,
            durationMinutes = sampleEpisode.durationMinutes
        )

        uiState = uiState.copy(activePlayback = playback, isPlaying = true)

        assertNotNull(uiState.activePlayback)
        assertEquals("ep_play_test", uiState.activePlayback?.id)
        assertEquals("Test Streaming Episode", uiState.activePlayback?.title)
        assertEquals("https://example.com/test_stream.m3u8", uiState.activePlayback?.streamUrl)
        assertFalse("Episodes are on-demand, not live streams", uiState.activePlayback!!.isLive)
        assertTrue(uiState.isPlaying)

        // Navigation to Screen.EPISODES
        uiState = uiState.copy(currentScreen = Screen.EPISODES)
        assertEquals(Screen.EPISODES, uiState.currentScreen)
    }
}

