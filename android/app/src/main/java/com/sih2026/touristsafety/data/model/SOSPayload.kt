package com.sih2026.touristsafety.data.model

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

data class SOSPayload(
    val latitude: Double,
    val longitude: Double,
    val userId: String,
    val timestamp: Long,
    val sosType: Byte
) {
    fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(PAYLOAD_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(latitude.toFloat())
        buffer.putFloat(longitude.toFloat())
        buffer.putInt(userId.hashCode())
        val timeOffset = (timestamp / 1000 - EPOCH_OFFSET).toInt()
        buffer.putInt(timeOffset)
        buffer.put(sosType)
        return buffer.array()
    }

    companion object {
        const val PAYLOAD_SIZE = 17
        val SERVICE_UUID: UUID = UUID.fromString("0000FE26-0000-1000-8000-00805F9B34FB")
        const val MANUFACTURER_ID = 0x2026
        private const val EPOCH_OFFSET = 1704067200L // 2024-01-01 00:00:00 UTC in seconds

        fun fromBytes(bytes: ByteArray): SOSPayload {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val lat = buffer.float.toDouble()
            val lon = buffer.float.toDouble()
            val userIdHash = buffer.int.toString()
            val timeOffset = buffer.int
            val type = buffer.get()
            
            val timestamp = (timeOffset.toLong() + EPOCH_OFFSET) * 1000L
            
            return SOSPayload(
                latitude = lat,
                longitude = lon,
                userId = userIdHash,
                timestamp = timestamp,
                sosType = type
            )
        }
    }
}
