package com.yusufyilmaz00.patienttracker.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID

// ESP32 UUID'leri
private const val SERVICE_UUID = "12345678-1234-1234-1234-1234567890ab"
private const val CHARACTERISTIC_UUID = "abcd1234-5678-90ab-cdef-1234567890ab"
// Standart BLE Bildirim Descriptor UUID'si (Sabittir, değişmez)
private const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

class BleManager(private val context: Context) {
    private val TAG = "BleManager"

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    // ViewModel'e veri gönderecek "kablo"lar
    var onConnectionStateChanged: ((Boolean, String) -> Unit)? = null
    var onDataReceived: ((String) -> Unit)? = null
    var onDevicesDiscovered: ((List<BluetoothDevice>) -> Unit)? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

    @SuppressLint("MissingPermission")
    fun startScanning() {
        Log.d(TAG,"startScanning() çağrıldı0")
        if (bluetoothAdapter?.isEnabled == false) {
            Log.e(TAG, "Bluetooth kapalı, tarama başlatılamadı.")
            onConnectionStateChanged?.invoke(false, "Bluetooth Kapalı")
            return
        }

        // Taramaya başlamadan önce eski listeyi temizle
        discoveredDevices.clear()
        // UI'a boş listeyi hemen göndererek ekranı temizle
        onDevicesDiscovered?.invoke(discoveredDevices)

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "BluetoothLeScanner alınamadı. Cihaz desteklemiyor olabilir.")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        onConnectionStateChanged?.invoke(false, "Cihaz Aranıyor...")

        Log.i(TAG, "Tarama başlatılıyor...")
        scanner.startScan(null, settings, scanCallback)

        // Taramanın 5 saniye sonra otomatik durdurmak istersem
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i(TAG, "7 saniyelik tarama süresi doldu, tarama durduruluyor.")
            stopScanning()
        }, 7000)
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                Log.d(TAG, "Cihaz bulundu! Ad: ${device.name ?: "Bilinmiyor"}, Adres: ${device.address}")
                if (!discoveredDevices.any { it.address == device.address }) {
                    Log.i(TAG, "Yeni cihaz listeye eklendi: ${device.name}")
                    // Listede yoksa ekle
                    discoveredDevices.add(device)
                    // Güncel listeyi ViewModel'e gönder
                    onDevicesDiscovered?.invoke(discoveredDevices.toList()) // Kopyasını gönder
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // 3. Tarama neden başarısız oldu?
            Log.e(TAG, "Tarama başarısız oldu! Hata Kodu: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        stopScanning()
        onConnectionStateChanged?.invoke(false, "Cihaz Bulundu, Bağlanılıyor...")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        // İsteğe bağlı: Tarama bitince durumu güncelle
        if (bluetoothGatt == null) { // Eğer bir cihaza bağlanmıyorsak
            onConnectionStateChanged?.invoke(false, "Tarama Tamamlandı.")
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnectionStateChanged?.invoke(true, "Bağlandı")
                gatt?.discoverServices() // Servisleri bulmazsak veri okuyamayız
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onConnectionStateChanged?.invoke(false, "Bağlantı Koptu")
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        // Servisler bulunduğunda çalışır
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

                if (characteristic != null) {
                    // 1. Android tarafında dinlemeyi aç
                    gatt.setCharacteristicNotification(characteristic, true)

                    // 2. ESP32'ye "Ben dinliyorum, veriyi gönder" emri ver (Descriptor Write)
                    val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }

        // ESP32'den veri geldiğinde burası çalışır
        // Bu metod Android 12 ve altı için kullanılır.
        @Deprecated("Kullanımdan kalktı, uyumluluk için burada.", ReplaceWith("onCharacteristicChanged(gatt, characteristic, characteristic.value)"))
        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)

            // Gelen byte verisini String'e çevirip ViewModel'e gönderelim.
            val dataBytes = characteristic.value
            if (dataBytes != null && dataBytes.isNotEmpty()) {
                val data = String(dataBytes, Charsets.UTF_8)
                Log.d(TAG, "Veri Alındı (Eski Metod): $data")
                onDataReceived?.invoke(data)
            }
        }

        // Android 13 ve sonrası için YENİ ve DOĞRU metod budur.
        // Sistem, veri geldiğinde artık bunu çağırır.
        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            val data = String(value, Charsets.UTF_8)
            Log.d(TAG, "Veri Alındı (Yeni Metod): $data")
            onDataReceived?.invoke(data)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
    }
}