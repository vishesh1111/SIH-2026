package com.sih2026.touristsafety.services.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleSOSAdvertiser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    
    private var isAdvertising = false
    private val _advertisingState = MutableStateFlow(false)
    val advertisingState: StateFlow<Boolean> = _advertisingState.asStateFlow()
    
    private val _acknowledgments = MutableSharedFlow<String>(replay = 5)
    val acknowledgments: SharedFlow<String> = _acknowledgments.asSharedFlow()
    
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stopAdvertising() }

    companion object {
        val ACK_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FE27-0000-1000-8000-00805F9B34FB")
    }

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

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (characteristic.uuid == ACK_CHARACTERISTIC_UUID) {
                val message = String(value, Charsets.UTF_8)
                CoroutineScope(Dispatchers.IO).launch {
                    _acknowledgments.emit(message)
                }
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(payload: SOSPayload) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (advertiser == null) return
        
        stopAdvertising() // Ensure we stop the old payload before broadcasting the new one

        // 1. Start GATT Server to accept connections
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(SOSPayload.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val ackCharacteristic = BluetoothGattCharacteristic(
            ACK_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(ackCharacteristic)
        gattServer?.addService(service)

        // 2. Start Advertising (Connectable)
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true) // Crucial for two-way GATT
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
        gattServer?.close()
        gattServer = null
        handler.removeCallbacks(stopRunnable)
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
}
