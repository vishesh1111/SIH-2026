package com.sih2026.touristsafety.services.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.sih2026.touristsafety.data.local.dao.ReceivedSOSAlertDao
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity
import com.sih2026.touristsafety.data.model.SOSPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

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

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                val manufacturerData = scanResult.scanRecord?.getManufacturerSpecificData(SOSPayload.MANUFACTURER_ID)
                manufacturerData?.let { bytes ->
                    scope.launch {
                        try {
                            val payload = SOSPayload.fromBytes(bytes)
                            val hash = payload.userId
                            
                            val recentCount = alertDao.countRecentAlerts(
                                userIdHash = hash,
                                sinceTimestamp = System.currentTimeMillis() - 5 * 60 * 1000
                            )
                            
                            if (recentCount == 0) {
                                val entity = ReceivedSOSAlertEntity(
                                    victimUserIdHash = hash,
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

        scanner?.startScan(listOf(filter), settings, scanCallback)
        isScanning = true
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (isScanning) {
            scanner?.stopScan(scanCallback)
            isScanning = false
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
}
