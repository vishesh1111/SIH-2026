package com.sih2026.touristsafety.services

import kotlin.math.log10

object AudioProcessor {
    // Basic constants
    private const val FRAME_SIZE_MS = 25
    private const val HOP_SIZE_MS = 10
    
    fun extractMFCCs(audioData: ShortArray, sampleRate: Int): FloatArray {
        // Deterministic pseudo-MFCC based on audio
        val result = FloatArray(40)
        val chunk = audioData.size / 40
        if (chunk == 0) return result
        for (i in 0 until 40) {
            var sum = 0.0
            for (j in 0 until chunk) {
                val idx = i * chunk + j
                if (idx < audioData.size) {
                    sum += Math.abs(audioData[idx].toInt())
                }
            }
            result[i] = (sum / chunk).toFloat() / 3276.8f
        }
        return result
    }

    fun extractMelSpectrogram(audioData: ShortArray, sampleRate: Int): FloatArray {
        // Deterministic pseudo-Mel based on audio
        val result = FloatArray(128)
        val chunk = audioData.size / 128
        if (chunk == 0) return result
        for (i in 0 until 128) {
            var sum = 0.0
            for (j in 0 until chunk) {
                val idx = i * chunk + j
                if (idx < audioData.size) {
                    sum += Math.abs(audioData[idx].toInt())
                }
            }
            result[i] = (sum / chunk).toFloat() / 327.68f
        }
        return result
    }

    fun calculateEnergy(frame: ShortArray): Double {
        var sumSq = 0.0
        for (sample in frame) {
            val normSample = sample / 32768.0
            sumSq += normSample * normSample
        }
        return sumSq / frame.size
    }

    fun estimatePitch(audioData: ShortArray, sampleRate: Int): Float {
        var maxVal = 0.0
        var maxLag = 0
        // Human pitch is typically 80Hz - 300Hz
        val minLag = sampleRate / 300
        val maxLagLimit = sampleRate / 80
        
        val windowSize = Math.min(2048, audioData.size - maxLagLimit)
        if (windowSize <= 0) return 0f

        for (lag in minLag..maxLagLimit) {
            var sum = 0.0
            for (i in 0 until windowSize) {
                sum += (audioData[i] / 32768.0) * (audioData[i + lag] / 32768.0)
            }
            if (sum > maxVal) {
                maxVal = sum
                maxLag = lag
            }
        }
        return if (maxLag == 0) 0f else sampleRate.toFloat() / maxLag
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
