package com.sih2026.touristsafety.services

import kotlin.math.log10

object AudioProcessor {
    // Basic constants
    private const val FRAME_SIZE_MS = 25
    private const val HOP_SIZE_MS = 10
    
    fun extractMFCCs(audioData: ShortArray, sampleRate: Int): FloatArray {
        // MOCK IMPLEMENTATION of MFCC Extraction
        // In a real scenario, we would implement or use a library (like JTransforms or TarsosDSP)
        // to compute STFT, apply Mel filter banks, log, and DCT.
        // Return 40 mock MFCC features as required by the model.
        return FloatArray(40) { i -> (Math.random() * 10 - 5).toFloat() }
    }

    fun extractMelSpectrogram(audioData: ShortArray, sampleRate: Int): FloatArray {
        // MOCK IMPLEMENTATION
        // Return 128-dimensional mean Mel spectrogram
        return FloatArray(128) { i -> (Math.random() * 100).toFloat() }
    }

    fun calculateEnergy(frame: ShortArray): Double {
        var sumSq = 0.0
        for (sample in frame) {
            val normSample = sample / 32768.0 // normalize to [-1, 1]
            sumSq += normSample * normSample
        }
        return sumSq / frame.size
    }

    fun isVoiceDetected(audioData: ShortArray, sampleRate: Int, energyThreshold: Double = 0.001, minDurationSeconds: Double = 0.5): Boolean {
        // Ported from vad() in main.py
        val frameSize = 1024
        val numFrames = audioData.size / frameSize
        
        var activeFrames = 0
        var speechDetected = false
        
        for (i in 0 until numFrames) {
            val frame = audioData.copyOfRange(i * frameSize, (i + 1) * frameSize)
            val energy = calculateEnergy(frame)
            if (energy > energyThreshold) {
                speechDetected = true
                activeFrames++
            }
        }
        
        if (speechDetected) {
            val duration = activeFrames * (frameSize.toDouble() / sampleRate)
            return duration >= minDurationSeconds
        }
        return false
    }
}
