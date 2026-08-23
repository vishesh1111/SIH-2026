package com.sih2026.touristsafety.services.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import com.sih2026.touristsafety.data.local.dao.ReceivedSOSAlertDao
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity
import com.sih2026.touristsafety.data.model.SOSPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class BleSOSScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alertDao: ReceivedSOSAlertDao
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var scanner: BluetoothLeScanner? = null
    private var isScanning = false
    private val _scannedAlerts = MutableSharedFlow<ReceivedSOSAlertEntity>(replay = 5)
    val scannedAlerts: SharedFlow<ReceivedSOSAlertEntity> = _scannedAlerts.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Map to keep track of discovered devices in memory for GATT connection
    private val deviceMap = mutableMapOf<String, BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                val manufacturerData = scanResult.scanRecord?.getManufacturerSpecificData(SOSPayload.MANUFACTURER_ID)
                manufacturerData?.let { bytes ->
                    scope.launch {
                        try {
                            val payload = SOSPayload.fromBytes(bytes)
                            val hash = payload.userId
                            
                            // Store the device reference
                            deviceMap[hash] = scanResult.device
                            
                            val recentCount = alertDao.countRecentAlerts(
                                userIdHash = hash,
                                sinceTimestamp = System.currentTimeMillis() - 5 * 60 * 1000
                            )
                            
                            if (recentCount == 0) {
                                // Extract the device name broadcasted by setIncludeDeviceName(true)
                                val deviceName = scanResult.device.name ?: scanResult.scanRecord?.deviceName

                                val entity = ReceivedSOSAlertEntity(
                                    victimUserIdHash = hash,
                                    victimName = deviceName,
                                    latitude = payload.latitude,
                                    longitude = payload.longitude,
                                    sosType = payload.sosType.toInt(),
                                    sosTimestamp = payload.timestamp,
                                    rssi = scanResult.rssi
                                )
                                alertDao.insert(entity)
                                _scannedAlerts.emit(entity)
                            }
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun acknowledgeAlert(userIdHash: String, helperInfo: String, onResult: (Boolean) -> Unit) {
        val device = deviceMap[userIdHash]
        if (device == null) {
            android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(false) }
            return
        }

        var isDone = false
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.requestMtu(512) // Request max MTU to avoid 20-byte truncation
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (!isDone) {
                        isDone = true
                        android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(false) }
                    }
                    gatt.close()
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                gatt.discoverServices() // Proceed to discover services after MTU is updated
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SOSPayload.SERVICE_UUID)
                    val ackChar = service?.getCharacteristic(UUID.fromString("0000FE27-0000-1000-8000-00805F9B34FB"))
                    
                    if (ackChar != null) {
                        ackChar.value = helperInfo.toByteArray(Charsets.UTF_8)
                        val enqueued = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeCharacteristic(ackChar, helperInfo.toByteArray(Charsets.UTF_8), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) == 0
                        } else {
                            ackChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                            gatt.writeCharacteristic(ackChar)
                        }
                        if (!enqueued) {
                            if (!isDone) {
                                isDone = true
                                android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(false) }
                            }
                            gatt.disconnect()
                        }
                    } else {
                        if (!isDone) {
                            isDone = true
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(false) }
                        }
                        gatt.disconnect()
                    }
                } else {
                    if (!isDone) {
                        isDone = true
                        android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(false) }
                    }
                    gatt.disconnect()
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (!isDone) {
                    isDone = true
                    val success = (status == BluetoothGatt.GATT_SUCCESS)
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(success) }
                }
                gatt.disconnect()
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        } catch (e: SecurityException) {
            android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(false) }
        }
        
        // Timeout mechanism
        scope.launch {
            delay(10000) // 10 seconds timeout
            if (!isDone) {
                isDone = true
                onResult(false)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null || isScanning) return

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SOSPayload.SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .build()

        try {
            scanner?.startScan(listOf(filter), settings, scanCallback)
            isScanning = true
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Permission not granted yet, will be restarted after prompt
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (isScanning) {
                scanner?.stopScan(scanCallback)
                isScanning = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
}
