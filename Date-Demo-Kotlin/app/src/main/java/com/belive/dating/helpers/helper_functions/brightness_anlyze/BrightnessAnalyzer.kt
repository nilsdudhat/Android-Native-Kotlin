package com.belive.dating.helpers.helper_functions.brightness_anlyze

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class BrightnessAnalyzer(
    private val onLowLightDetected: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastProcessedTime = 0L
    private val cooldown = 2000L // 2 seconds between checks

    override fun analyze(image: ImageProxy) {
        if (System.currentTimeMillis() - lastProcessedTime < cooldown) {
            image.close()
            return
        }

        val brightness = calculateBrightness(image)
        onLowLightDetected(brightness < 0.25f) // 0-1 scale (0.25 = dark)

        lastProcessedTime = System.currentTimeMillis()
        image.close()
    }

    private fun calculateBrightness(image: ImageProxy): Float {
        val buffer = image.planes[0].buffer
        val pixelCount = buffer.remaining() / 3 // For YUV format
        var sum = 0f

        // Simplified luminance calculation for Y channel
        while (buffer.remaining() > 0) {
            sum += buffer.get().toInt() and 0xFF
        }
        return sum / (pixelCount * 255f) // Normalized 0-1
    }
}