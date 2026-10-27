package com.example.tamilstream

import com.example.tamilstream.data.model.AdConfig
import com.example.tamilstream.data.repository.TamilStreamRepository
import org.junit.Assert.*
import org.junit.Test

class AdConfigTest {

    @Test
    fun testAdConfigVisibilityRules() {
        val activeAd = AdConfig(
            htmlContent = "<div>Test Banner Ad</div>",
            enabled = true,
            topEnabled = true,
            bottomEnabled = true
        )
        assertTrue("Master enabled Ad with non-blank HTML must be visible", activeAd.isVisible)
        assertTrue("Top placement must be visible when enabled", activeAd.isTopVisible)
        assertTrue("Bottom placement must be visible when enabled", activeAd.isBottomVisible)

        val topDisabledAd = AdConfig(
            htmlContent = "<div>Test Banner Ad</div>",
            enabled = true,
            topEnabled = false,
            bottomEnabled = true
        )
        assertTrue("Master enabled is true", topDisabledAd.isVisible)
        assertFalse("Top placement must NOT be visible when topEnabled is false", topDisabledAd.isTopVisible)
        assertTrue("Bottom placement remains visible when bottomEnabled is true", topDisabledAd.isBottomVisible)

        val disabledAd = AdConfig(
            htmlContent = "<div>Test Banner Ad</div>",
            enabled = false,
            topEnabled = true,
            bottomEnabled = true
        )
        assertFalse("Disabled master Ad must NOT be visible even if placement enabled", disabledAd.isVisible)
        assertFalse("Top placement must NOT be visible when master disabled", disabledAd.isTopVisible)
        assertFalse("Bottom placement must NOT be visible when master disabled", disabledAd.isBottomVisible)

        val emptyHtmlAd = AdConfig(
            htmlContent = "   ",
            enabled = true,
            topEnabled = true,
            bottomEnabled = true
        )
        assertFalse("Ad with blank HTML code must NOT be visible even if enabled", emptyHtmlAd.isVisible)
        assertFalse("Top placement must NOT be visible when HTML is blank", emptyHtmlAd.isTopVisible)
        assertFalse("Bottom placement must NOT be visible when HTML is blank", emptyHtmlAd.isBottomVisible)
    }

    @Test
    fun testSaveAndPublishAdWorkflow() {
        val repo = TamilStreamRepository()
        val initialAd = repo.adConfig.value
        assertNotNull("Initial Ad configuration must not be null", initialAd)

        val newHtmlCode = """
            <script>
              atOptions = {
                'key': 'fe316113edd8cf0caf21d5be2911485c',
                'format': 'iframe',
                'height': 90,
                'width': 728,
                'params': {}
              };
            </script>
            <script src="https://bancadeltempoidea.org/22/fe316113edd8cf0caf21d5be2911485c"></script>
        """.trimIndent()

        val saveResult = repo.saveAdConfig(
            htmlContent = newHtmlCode,
            enabled = true,
            topEnabled = true,
            bottomEnabled = true,
            adminUid = "admin_root_master"
        )
        assertTrue("Save Ad Config must return success", saveResult.isSuccess)

        val updatedAd = repo.adConfig.value
        assertEquals(newHtmlCode.trim(), updatedAd.htmlContent)
        assertTrue(updatedAd.enabled)
        assertTrue(updatedAd.topEnabled)
        assertTrue(updatedAd.bottomEnabled)
        assertTrue(updatedAd.isVisible)
        assertTrue(updatedAd.isTopVisible)
        assertTrue(updatedAd.isBottomVisible)
        assertEquals("admin_root_master", updatedAd.updatedBy)
        assertTrue(updatedAd.version > initialAd.version)
    }

    @Test
    fun testToggleAdPlacementWorkflow() {
        val repo = TamilStreamRepository()

        // 1. Enable ad with content
        repo.saveAdConfig("<div>Promo Ad</div>", enabled = true, topEnabled = true, bottomEnabled = true, adminUid = "admin_root_master")
        assertTrue(repo.adConfig.value.isTopVisible)
        assertTrue(repo.adConfig.value.isBottomVisible)

        // 2. Toggle top placement disabled
        repo.toggleAdTopEnabled("admin_root_master")
        assertFalse(repo.adConfig.value.topEnabled)
        assertFalse(repo.adConfig.value.isTopVisible)
        assertTrue(repo.adConfig.value.isBottomVisible)

        // 3. Toggle bottom placement disabled
        repo.toggleAdBottomEnabled("admin_root_master")
        assertFalse(repo.adConfig.value.bottomEnabled)
        assertFalse(repo.adConfig.value.isBottomVisible)

        // 4. Toggle top back on
        repo.toggleAdTopEnabled("admin_root_master")
        assertTrue(repo.adConfig.value.topEnabled)
        assertTrue(repo.adConfig.value.isTopVisible)
        assertFalse(repo.adConfig.value.isBottomVisible)

        // 5. Clear Ad
        repo.clearAdConfig("admin_root_master")
        assertEquals("", repo.adConfig.value.htmlContent)
        assertFalse(repo.adConfig.value.enabled)
        assertFalse(repo.adConfig.value.isTopVisible)
        assertFalse(repo.adConfig.value.isBottomVisible)
    }
}

