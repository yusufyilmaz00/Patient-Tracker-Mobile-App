package com.yusufyilmaz00.patienttracker

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufyilmaz00.patienttracker.data.ble.BleManager
import com.yusufyilmaz00.patienttracker.ui.screen.monitor.MonitorNotification
import com.yusufyilmaz00.patienttracker.ui.screen.monitor.MonitorUiState
import com.yusufyilmaz00.patienttracker.ui.screen.monitor.NotificationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Bu ViewModel, uygulama boyunca tek bir örnek olarak yaşayacak.
class MainViewModel : ViewModel() {

    // Monitor ekranının state'ini artık burada, merkezi bir yerde tutacağız.
    private val _monitorUiState = MutableStateFlow(MonitorUiState())
    val monitorUiState = _monitorUiState.asStateFlow()

    private val TAG = "MainViewModel"
    private var bleManager: BleManager? = null
    // 1. Örnek Verileri Hazırla
    val sampleNotifications = listOf(
        MonitorNotification(NotificationType.EMERGENCY, "Emergency button pressed", "1 minute ago"),
        MonitorNotification(NotificationType.FALL_DETECTED, "Fall detected", "3 minutes ago"),
        MonitorNotification(NotificationType.WARNING, "Heart rate exceeded 95 bpm", "15 minutes ago"),
        MonitorNotification(NotificationType.INFO, "Heart rate returned to normal", "2 hours ago"),
        MonitorNotification(NotificationType.SUCCESS, "Daily check-in complete", "5 hours ago")
    )



    init {
        _monitorUiState.update { it.copy(notifications = sampleNotifications) }
    }

    fun initBleConnection(context: Context) {
        if (bleManager == null) {
            Log.d(TAG, "initBleConnection çağrıldı. BleManager oluşturuluyor.")
            bleManager = BleManager(context)

            // 1. Bağlantı Durumu Değiştiğinde
            bleManager?.onConnectionStateChanged = { isConnected, statusMsg ->
                viewModelScope.launch {
                    _monitorUiState.update {
                        it.copy(
                            isDeviceConnected = isConnected,
                            connectionStatus = statusMsg
                        )
                    }
                }
            }

            // 2. Veri Geldiğinde (Asıl Olay Burası)
            bleManager?.onDataReceived = { rawData ->
                viewModelScope.launch {
                    processIncomingData(rawData)
                }
            }

            bleManager?.onDevicesDiscovered = { devices ->
                viewModelScope.launch {
                    Log.d(TAG, "onDevicesDiscovered tetiklendi. Bulunan cihaz sayısı: ${devices.size}")
                    _monitorUiState.update { it.copy(scannedDevices = devices) }
                }
            }
        }
    }

    // Gelen String veriyi (g:0.98 IR:52000...) parse etme
    private fun processIncomingData(rawData: String) {

        // Acil durum butonu kontrolü
        if (rawData.contains("ACIL DURUM")) {
            _monitorUiState.update {
                it.copy(
                    showEmergencyPopup = true,
                    notifications = it.notifications + MonitorNotification(
                        NotificationType.EMERGENCY, "Acil Durum Butonu!", getCurrentTime()
                    )
                )
            }
            return
        }

        // Regex ile BPM'i çekelim
        val bpm = Regex("BPM:([\\d\\.]+)").find(rawData)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f

        // Uyarı bayrakları
        val isFall = rawData.contains("FALL!")
        val isFaint = rawData.contains("BAYGINLIK!")

        _monitorUiState.update { state ->
            // Eğer yeni bir uyarı varsa listeye ekle
            var newNotifications = state.notifications
            if (isFall) {
                newNotifications = newNotifications + MonitorNotification(NotificationType.FALL_DETECTED, "Düşme Algılandı!", getCurrentTime())
            }
            if (isFaint) {
                newNotifications = newNotifications + MonitorNotification(NotificationType.WARNING, "Baygınlık Şüphesi!", getCurrentTime())
            }

            // State'i güncelle
            state.copy(
                heartRate = bpm.toInt(),
                heartRateHistory = state.heartRateHistory + bpm.toInt(), // Grafiğe veri ekle
                showEmergencyPopup = isFall || isFaint || state.showEmergencyPopup, // Düşme varsa popup aç
                notifications = newNotifications
            )
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        bleManager?.connectToDevice(device)
    }

    fun startBleScan() {
        Log.d(TAG,"startBleScan() çağrıldı.")
        bleManager?.startScanning()
    }

    fun disconnectBle() {
        bleManager?.disconnect()
    }

    fun dismissEmergencyAlert() {
        _monitorUiState.update { it.copy(showEmergencyPopup = false) }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun startListeningForDeviceData() {
        // TODO: Burada Bluetooth/Cihaz servisinden gelen gerçek veriyi dinle.
        // Örnek olarak nabız verisini güncelleyelim:
        viewModelScope.launch {
            while (true) {
                delay(2000) // Her 2 saniyede bir nabız güncelleniyor gibi simüle et
                val randomHeartRate = (65..85).random()
                _monitorUiState.update { it.copy(heartRate = randomHeartRate) }
            }
        }
    }
}
