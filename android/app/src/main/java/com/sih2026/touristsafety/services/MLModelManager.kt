package com.sih2026.touristsafety.services

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

/**
 * MLModelManager – real on-device ML inference for the Safety Monitor.
 *
 * • Scream Phase 1 (SVM, linear kernel)  → loaded from svm_phase1.json
 * • Scream Phase 2 (SVM, RBF kernel)     → loaded from svm_phase2.json
 * • Gender         (Dense NN, TFLite)    → loaded from gender.tflite
 * • Distress       (Dense NN, TFLite)    → loaded from distress.tflite
 */
object MLModelManager {

    // ── TFLite interpreters ──────────────────────────────────────
    private var genderInterpreter: Interpreter? = null
    private var distressInterpreter: Interpreter? = null

    // ── SVM parameters ───────────────────────────────────────────
    private var phase1Params: SVMParams? = null
    private var phase2Params: SVMParams? = null

    private var isInitialized = false

    /**
     * Data class that holds the exported parameters of an SVM (SVC).
     */
    data class SVMParams(
        val kernel: String,
        val gamma: Double,
        val classes: DoubleArray,
        val supportVectors: Array<DoubleArray>,
        val dualCoef: Array<DoubleArray>,
        val intercept: DoubleArray
    )

    // ─────────────────────────────────────────────────────────────
    // Initialization (call once from Application or Service)
    // ─────────────────────────────────────────────────────────────
    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) return
        val assets = context.assets

        // Load SVM parameters from JSON
        phase1Params = loadSVMParams(assets, "models/svm_phase1.json")
        phase2Params = loadSVMParams(assets, "models/svm_phase2.json")

        // Load TFLite models
        genderInterpreter = try {
            Interpreter(loadModelFile(assets, "models/gender.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        distressInterpreter = try {
            Interpreter(loadModelFile(assets, "models/distress.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        isInitialized = true
    }

    // ─────────────────────────────────────────────────────────────
    // Public inference methods (same signatures as before)
    // ─────────────────────────────────────────────────────────────

    /**
     * Phase 1: Noise (class 1) vs Human (class 2).
     * Returns the predicted class label (1 or 2).
     */
    fun runScreamPhase1Inference(interpreter: Interpreter?, input: FloatArray): Int {
        val params = phase1Params ?: return 1 // fallback to noise
        return svmPredict(params, input).toInt()
    }

    /**
     * Phase 2: Scream (class 1) vs Speech (class 2).
     * Returns the predicted class label.
     */
    fun runScreamPhase2Inference(interpreter: Interpreter?, input: FloatArray): Int {
        val params = phase2Params ?: return 2 // fallback to speech
        return svmPredict(params, input).toInt()
    }

    /**
     * Gender inference via TFLite.
     * Input: 128-d Mel spectrogram features.
     * Returns: male probability (sigmoid output).
     */
    fun runGenderInference(interpreter: Interpreter?, input: FloatArray): Float {
        val interp = genderInterpreter ?: return 0.5f
        val inputBuffer = ByteBuffer.allocateDirect(input.size * 4).apply {
            order(ByteOrder.nativeOrder())
            input.forEach { putFloat(it) }
        }
        val output = Array(1) { FloatArray(1) }
        interp.run(inputBuffer, output)
        return output[0][0]
    }

    /**
     * Distress inference via TFLite.
     * Input: 13248 raw audio samples (normalized float).
     * Returns: distress probability (sigmoid output).
     */
    fun runDistressInference(interpreter: Interpreter?, input: FloatArray): Float {
        val interp = distressInterpreter ?: return 0f
        val inputBuffer = ByteBuffer.allocateDirect(input.size * 4).apply {
            order(ByteOrder.nativeOrder())
            input.forEach { putFloat(it) }
        }
        val output = Array(1) { FloatArray(1) }
        interp.run(inputBuffer, output)
        return output[0][0]
    }

    // ─────────────────────────────────────────────────────────────
    // SVM prediction (supports linear and rbf kernels)
    // ─────────────────────────────────────────────────────────────
    private fun svmPredict(params: SVMParams, input: FloatArray): Double {
        val x = input.map { it.toDouble() }.toDoubleArray()
        val sv = params.supportVectors
        val dualCoef = params.dualCoef[0] // shape [n_sv] for binary SVM
        val intercept = params.intercept[0]

        var decision = intercept
        for (i in sv.indices) {
            val kernelVal = when (params.kernel) {
                "linear" -> dotProduct(x, sv[i])
                "rbf" -> rbfKernel(x, sv[i], params.gamma)
                else -> dotProduct(x, sv[i])
            }
            decision += dualCoef[i] * kernelVal
        }

        // For binary SVM: decision > 0 → class at index 1, else class at index 0
        return if (decision > 0) params.classes[1] else params.classes[0]
    }

    private fun dotProduct(a: DoubleArray, b: DoubleArray): Double {
        var sum = 0.0
        for (i in a.indices) {
            sum += a[i] * b[i]
        }
        return sum
    }

    private fun rbfKernel(x: DoubleArray, sv: DoubleArray, gamma: Double): Double {
        var sqDist = 0.0
        for (i in x.indices) {
            val diff = x[i] - sv[i]
            sqDist += diff * diff
        }
        return exp(-gamma * sqDist)
    }

    // ─────────────────────────────────────────────────────────────
    // Asset loading helpers
    // ─────────────────────────────────────────────────────────────
    private fun loadModelFile(assets: AssetManager, filename: String): MappedByteBuffer {
        val fd = assets.openFd(filename)
        val inputStream = FileInputStream(fd.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    private fun loadSVMParams(assets: AssetManager, filename: String): SVMParams? {
        return try {
            val jsonStr = assets.open(filename).bufferedReader().use { it.readText() }
            val json = JSONObject(jsonStr)

            val kernel = json.getString("kernel")
            val gamma = json.getDouble("gamma")

            val classesArr = json.getJSONArray("classes")
            val classes = DoubleArray(classesArr.length()) { classesArr.getDouble(it) }

            val svArr = json.getJSONArray("support_vectors")
            val supportVectors = Array(svArr.length()) { i ->
                val row = svArr.getJSONArray(i)
                DoubleArray(row.length()) { j -> row.getDouble(j) }
            }

            val dcArr = json.getJSONArray("dual_coef")
            val dualCoef = Array(dcArr.length()) { i ->
                val row = dcArr.getJSONArray(i)
                DoubleArray(row.length()) { j -> row.getDouble(j) }
            }

            val intArr = json.getJSONArray("intercept")
            val intercept = DoubleArray(intArr.length()) { intArr.getDouble(it) }

            SVMParams(kernel, gamma, classes, supportVectors, dualCoef, intercept)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
