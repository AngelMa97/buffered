package com.angelma.feature.player.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.angelma.feature.player.presentation.util.qualityLabel
import org.junit.Test

class QualityLabelTest {

    @Test
    fun `widescreen 1920x1080 is 1080p`() {
        assertThat(qualityLabel(
            width = 1920,
            height = 1080
        )).isEqualTo("1080p")
    }

    @Test
    fun `widescreen 1920x818 is 1080p not 818p`() {
        assertThat(qualityLabel(
            width = 1920,
            height = 818
        )).isEqualTo("1080p")
    }

    @Test
    fun `exactly 1280x720 is 720p`() {
        assertThat(qualityLabel(width = 1280, height = 720)).isEqualTo("720p")
    }


    @Test
    fun `widescreen 1280x544 is 720p not 544p`() {
        assertThat(qualityLabel(
            width = 1280,
            height = 544
        )).isEqualTo("720p")
    }

    @Test
    fun `4x3 640x480 is 480p`() {
        assertThat(qualityLabel(
            width = 640,
            height = 480
        )).isEqualTo("480p")
    }

    @Test
    fun `4x3 608x448 is 480p not 448p`() {
        assertThat(qualityLabel(
            width = 608,
            height = 448
        )).isEqualTo("480p")
    }
}