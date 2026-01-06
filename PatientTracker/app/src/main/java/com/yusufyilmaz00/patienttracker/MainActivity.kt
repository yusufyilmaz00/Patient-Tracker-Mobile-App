package com.yusufyilmaz00.patienttracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.yusufyilmaz00.patienttracker.ui.navigation.AppNavGraph
import com.yusufyilmaz00.patienttracker.ui.theme.PatientTrackerTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            // İzinler tamam, BLE motorunu çalıştır
            mainViewModel.initBleConnection(this)
            Toast.makeText(this, "Bluetooth Bağlantısı Hazır", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "İzinler reddedildi, cihaz bağlanamaz!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            PatientTrackerTheme {
                AppNavGraph(mainViewModel = mainViewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Android 11 ve altı
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        permissionLauncher.launch(permissionsToRequest)
    }
}