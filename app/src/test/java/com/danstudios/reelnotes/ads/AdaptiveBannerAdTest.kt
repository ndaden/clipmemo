package com.danstudios.reelnotes.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AdaptiveBannerAdTest {

    @Test
    fun testBannerAdUnitIdIsAvailable() {
        val id = AdConfig.bannerAdUnitId
        assertNotNull(id)
        assertEquals(AdConfig.getBannerId(), id)
    }
}
