package com.yusufyilmaz00.patienttracker.ui.screen.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yusufyilmaz00.patienttracker.ui.screen.monitor.MonitorUiState
import com.yusufyilmaz00.patienttracker.ui.theme.BgLight
import com.yusufyilmaz00.patienttracker.ui.theme.NeutralBg
import com.yusufyilmaz00.patienttracker.ui.theme.OrangeBg
import com.yusufyilmaz00.patienttracker.ui.theme.OrangeText
import com.yusufyilmaz00.patienttracker.ui.theme.PrimaryBlue
import com.yusufyilmaz00.patienttracker.ui.theme.SurfaceWhite
import com.yusufyilmaz00.patienttracker.ui.theme.TextMain
import com.yusufyilmaz00.patienttracker.ui.theme.TextSub


data class DeviceItem(
    val name: String,
    val signalStrength: String,
    val icon: ImageVector
)

@SuppressLint("MissingPermission")
@Composable
fun MyDevicesUi(
    state: MonitorUiState,
    onScanClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onBackClick: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        HeaderSection(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(32.dp))

        ConnectionStatusCard(
            isConnected = state.isDeviceConnected,
            statusText = state.connectionStatus,
            onDisconnect = onDisconnectClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isDeviceConnected) {
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, CircleShape, spotColor = PrimaryBlue.copy(alpha = 0.4f)),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = CircleShape
            ) {
                // Tarama yapılıyorsa dönen loading ikonu
                if (state.connectionStatus.contains("Aranıyor") || state.connectionStatus.contains("Bulundu")) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Rounded.BluetoothSearching, contentDescription = null)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (state.connectionStatus.contains("Aranıyor")) "Scanning..." else "Scan for Devices",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Cihaz Listesi Başlığı
        // CİHAZ LİSTESİNİ GÖSTEREN BÖLÜM
        if (state.scannedDevices.isEmpty() && state.connectionStatus.contains("Aranıyor")) {
            // Henüz cihaz bulunamadıysa mesaj göster
            Text("Yakındaki cihazlar aranıyor...", color = TextSub, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Bulunan cihazları listele
            state.scannedDevices.forEach { device ->
                DeviceListItem(
                    device = device,
                    onConnectClick = { onDeviceClick(device) } // Tıklamayı ViewModel'e ilet
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Bilgi Kutusu (Info Box)
        InfoBox()

        Spacer(modifier = Modifier.height(40.dp)) // Alt boşluk
    }
}

// --- Alt Bileşenler (Composables) ---

@SuppressLint("MissingPermission") // Cihaz adını okumak için izin gerekir
@Composable
fun DeviceListItem(
    device: BluetoothDevice,
    onConnectClick: () -> Unit // Tıklama olayı
) {
    // Row'u bir Card içerisine almak daha modern bir görünüm sağlar, ama
    // orijinal tasarıma sadık kalarak direkt Row ile devam edelim.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. İkon Alanı
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Bluetooth, // Genel bir Bluetooth ikonu
                contentDescription = "Bluetooth Device",
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Yazı Alanı (Cihaz Adı ve Adresi)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name ?: "Bilinmeyen Cihaz", // Cihaz adını göster
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )
            )
            Spacer(modifier = Modifier.height(2.dp)) // İki metin arasına hafif boşluk
            Text(
                text = device.address, // MAC adresini göster
                style = MaterialTheme.typography.bodySmall.copy(color = TextSub)
            )
        }

        // 3. Connect Butonu
        Button(
            onClick = onConnectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeutralBg,
                contentColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp),
            elevation = null // Düz bir görünüm için gölgeyi kaldır
        ) {
            Text(
                text = "Connect",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun HeaderSection(onBackClick: () -> Unit) { // Parametre eklendi
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick, // Action bağlandı
            modifier = Modifier.size(40.dp).offset(x = (-12).dp)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextMain, modifier = Modifier.size(28.dp))
        }
        Text("Device Connection", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextMain, fontSize = 32.sp))
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    statusText: String,
    onDisconnect: () -> Unit
) {
    // Tasarım aynı, sadece renkler duruma göre değişiyor
    val iconColor = if (isConnected) Color(0xFF16A34A) else Color(0xFF9CA3AF)
    val bgColor = if (isConnected) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Rounded.BluetoothConnected else Icons.Rounded.LinkOff,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isConnected) "Connected" else "Connection Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextMain)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText, // Dinamik metin
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSub, fontWeight = FontWeight.Normal),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDisconnect, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                    Text("Disconnect")
                }
            }
        }
    }
}

@Composable
fun DeviceListItem(device: DeviceItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // İkon Alanı
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = device.icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Yazı Alanı
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )
            )
            Text(
                text = device.signalStrength,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSub)
            )
        }

        // Connect Butonu
        Button(
            onClick = { /* Connect logic */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeutralBg,
                contentColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp),
            elevation = null
        ) {
            Text(
                text = "Connect",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun InfoBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(OrangeBg, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = "Info",
            tint = Color(0xFFF97316), // Orange-500
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Ensure your device is turned on and within 5 feet of your phone. If you don't see your device, try pressing the \"Scan for Devices\" button again.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = OrangeText,
                lineHeight = 20.sp
            )
        )
    }
}
