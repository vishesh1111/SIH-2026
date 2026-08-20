package com.sih2026.touristsafety.utils

object GeminiApiKeys {
    private val keys = listOf(
        "AQ.Ab8RN6JGYKOnGLc94I2k25tHqRddMQrHxO9h8Ye2lESB4CwsIQ",
        "AQ.Ab8RN6L62GIubl1cFVJKsqTqFjpmACnxT-VvajOj1-gK5ZGPwg",
        "AQ.Ab8RN6I5DLpj1UqKAWvTLpuLCpYzJWdojNQs0-rB3Bfr851iVg",
        "AQ.Ab8RN6IZPSRKqKjg5m8vVY-MFX4vfPfW31CIzeopP0Y29Ye6EA",
        "AQ.Ab8RN6J-T-ViXkEedcbns9h6GfqSxaSXuXAI8Gt3AWcqPiHUSA"
    )
    private var currentIndex = 0

    fun getNextKey(): String {
        val key = keys[currentIndex]
        currentIndex = (currentIndex + 1) % keys.size
        return key
    }
}
