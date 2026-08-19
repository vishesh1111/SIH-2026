package com.sih2026.touristsafety.services.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import com.sih2026.touristsafety.data.model.SOSPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleSOSAdvertiser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private val _advertisingState = MutableStateFlow(false)
    val advertisingState: StateFlow<Boolean> = _advertisingState.asStateFlow()
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stopAdvertising() }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            _advertisingState.value = true
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            _advertisingState.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(payload: SOSPayload) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (advertiser == null) return

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SOSPayload.SERVICE_UUID))
            .addManufacturerData(SOSPayload.MANUFACTURER_ID, payload.toBytes())
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)

        handler.removeCallbacks(stopRunnable)
        handler.postDelayed(stopRunnable, 30L * 60L * 1000L)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (isAdvertising) {
            advertiser?.stopAdvertising(advertiseCallback)
            isAdvertising = false
            _advertisingState.value = false
        }
        handler.removeCallbacks(stopRunnable)
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
}
