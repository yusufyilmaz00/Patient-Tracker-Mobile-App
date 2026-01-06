package com.yusufyilmaz00.patienttracker.ui.screen.monitor

import android.bluetooth.BluetoothDevice


enum class NotificationType {
    EMERGENCY, FALL_DETECTED, WARNING, INFO, SUCCESS
}

data class MonitorNotification(
    val type: NotificationType,
    val title: String,
    val time: String,
    val isRead: Boolean = false
)

// --- ANA STATE CLASS ---
// Ekrandaki tüm değişken veriler burada tutulur
data class MonitorUiState(
    val isDeviceConnected: Boolean = false,
    val connectionStatus: String = "Bağlantı Yok", // Ekrana bilgi basmak için
    val heartRate: Int = 72,
    val heartRateHistory: List<Int> = emptyList(), // İlerde grafik için gerçek veri tutabilirsin
    val notifications: List<MonitorNotification> = emptyList(),
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val showEmergencyPopup: Boolean = false // Pop-up'ın açık/kapalı durumu
)