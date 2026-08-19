package com.sih2026.touristsafety.services

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

/**
 * Turns the victim's phone into a physical attractor with:
 * 1. Max-brightness strobing screen (alternating red/white)
 * 2. Loudest possible siren sound (generated programmatically, no audio files needed)
 * 3. Flashlight SOS blink pattern (···---··· in Morse code)
 *
 * Zero infrastructure required — just a human within earshot or eyeshot.
 */
@Singleton
class PhysicalSignalService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private var sirenTrack: AudioTrack? = null
    private var flashlightJob: Job? = null
    private var sirenJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var originalBrightness: Int = -1
    private var originalBrightnessMode: Int = -1

    // Morse code SOS: ··· --- ···
    // Dot = 200ms, Dash = 600ms, gap between signals = 200ms, gap between letters = 600ms, gap between words = 1400ms
    private val DOT = 200L
    private val DASH = 600L
    private val SIGNAL_GAP = 200L
    private val LETTER_GAP = 600L
    private val WORD_GAP = 1400L

    // Siren parameters
    private val SAMPLE_RATE = 44100
    private val SIREN_LOW_FREQ = 600.0   // Hz
    private val SIREN_HIGH_FREQ = 1200.0 // Hz
    private val SIREN_SWEEP_DURATION_MS = 700L // one sweep up or down

    /**
     * Activates all physical signals simultaneously.
     * Call from a context that has a window (Activity) for screen strobe,
     * or pass null for windowParams to skip screen strobing.
     */
    fun activate(windowParams: WindowManager.LayoutParams? = null) {
        if (_isActive.value) return
        _isActive.value = true

        acquireWakeLock()
        startSiren()
        startFlashlightSOS()

        // Max brightness (best effort — needs WRITE_SETTINGS or an Activity window)
        if (windowParams != null) {
            setMaxBrightness(windowParams)
        }
    }

    /**
     * Stops all physical signals and restores device state.
     */
    fun deactivate(windowParams: WindowManager.LayoutParams? = null) {
        _isActive.value = false

        stopSiren()
        stopFlashlightSOS()
        releaseWakeLock()

        if (windowParams != null) {
            restoreBrightness(windowParams)
        }
    }

    // ========================================================================
    // 1. SIREN — Programmatically generated two-tone emergency siren
    // ========================================================================

    private fun startSiren() {
        sirenJob = scope.launch {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Set volume to max
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            sirenTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            sirenTrack?.play()

            // Generate continuous siren sweep
            while (isActive && _isActive.value) {
                // Sweep up: low → high
                generateSweep(SIREN_LOW_FREQ, SIREN_HIGH_FREQ, SIREN_SWEEP_DURATION_MS)
                // Sweep down: high → low
                generateSweep(SIREN_HIGH_FREQ, SIREN_LOW_FREQ, SIREN_SWEEP_DURATION_MS)
            }

            sirenTrack?.stop()
            sirenTrack?.release()
            sirenTrack = null
        }
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationMs: Long) {
        val numSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val sample = sin(2.0 * PI * freq * t)
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        sirenTrack?.write(buffer, 0, buffer.size)
    }

    private fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
        try {
            sirenTrack?.stop()
            sirenTrack?.release()
        } catch (_: Exception) {}
        sirenTrack = null
    }

    // ========================================================================
    // 2. FLASHLIGHT SOS — ···---··· Morse code blink pattern
    // ========================================================================

    @SuppressLint("MissingPermission")
    private fun startFlashlightSOS() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = findFlashlightCamera(cameraManager) ?: return

        flashlightJob = scope.launch {
            while (isActive && _isActive.value) {
                // S: · · ·
                repeat(3) {
                    flashOn(cameraManager, cameraId)
                    delay(DOT)
                    flashOff(cameraManager, cameraId)
                    delay(SIGNAL_GAP)
                }
                delay(LETTER_GAP - SIGNAL_GAP) // adjust for last signal gap

                // O: — — —
                repeat(3) {
                    flashOn(cameraManager, cameraId)
                    delay(DASH)
                    flashOff(cameraManager, cameraId)
                    delay(SIGNAL_GAP)
                }
                delay(LETTER_GAP - SIGNAL_GAP)

                // S: · · ·
                repeat(3) {
                    flashOn(cameraManager, cameraId)
                    delay(DOT)
                    flashOff(cameraManager, cameraId)
                    delay(SIGNAL_GAP)
                }

                // Pause before repeating
                delay(WORD_GAP)
            }
            // Ensure flash is off when stopped
            flashOff(cameraManager, cameraId)
        }
    }

    private fun findFlashlightCamera(cameraManager: CameraManager): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun flashOn(cameraManager: CameraManager, cameraId: String) {
        try {
            cameraManager.setTorchMode(cameraId, true)
        } catch (_: Exception) {}
    }

    private fun flashOff(cameraManager: CameraManager, cameraId: String) {
        try {
            cameraManager.setTorchMode(cameraId, false)
        } catch (_: Exception) {}
    }

    private fun stopFlashlightSOS() {
        flashlightJob?.cancel()
        flashlightJob = null
        // Best-effort turn off flash
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = findFlashlightCamera(cameraManager)
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (_: Exception) {}
    }

    // ========================================================================
    // 3. SCREEN BRIGHTNESS — Max brightness + strobe colors
    // ========================================================================

    /**
     * Sets screen to max brightness. Requires an Activity's window LayoutParams.
     * The SOSScreen composable should pass its Activity's window params.
     */
    private fun setMaxBrightness(layoutParams: WindowManager.LayoutParams) {
        try {
            // Save original brightness
            originalBrightnessMode = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
            originalBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
        } catch (_: Exception) {}

        // Force max brightness on the activity window
        handler.post {
            layoutParams.screenBrightness = 1.0f // max
        }
    }

    private fun restoreBrightness(layoutParams: WindowManager.LayoutParams) {
        handler.post {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    // ========================================================================
    // WAKE LOCK — Keep screen on during signaling
    // ========================================================================

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "TouristSafety::PhysicalSignal"
        )
        wakeLock?.acquire()
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.release()
        } catch (_: Exception) {}
        wakeLock = null
    }

    /**
     * Returns the strobe color to display on the SOS screen.
     * Alternates between red and white every 300ms when active.
     */
    fun getStrobeColors(): Pair<Int, Int> = Pair(Color.RED, Color.WHITE)
}
