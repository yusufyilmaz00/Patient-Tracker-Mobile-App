package com.yusufyilmaz00.patienttracker.ui.screen.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Renk Paleti ---
val PrimaryBlue = Color(0xFF137FEC)
val BgLight = Color(0xFFF6F7F8)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextMain = Color(0xFF0D141B)
val TextSub = Color(0xFF6B7280) // Gray-500
val DangerRed = Color(0xFFDC2626) // Red-600
val DangerBg = Color(0xFFFEF2F2) // Red-50
val DangerBorder = Color(0xFFFEE2E2) // Red-100

@Composable
fun SettingsUi() {
    // Switch durumu için basit bir state (Statik UI olsa bile switch'in çalışır görünmesi için)
    var isNotificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(bottom = 80.dp) // Bottom Bar boşluğu
    ) {
        // 1. Header (Başlık)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        )
        Text(
            text = "Uygulama tercihlerinizi yönetin",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSub,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Ayar Listesi
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Cihaz Yönetimi
            SettingsItemCard(
                title = "Cihaz Yönetimi",
                subtitle = "Saat durumu ve bağlantı",
                icon = Icons.Rounded.Watch,
                onClick = { /* Navigate to Device Mgmt */ }
            )

            // Bildirimler (Switch içerir)
            SettingsItemCard(
                title = "Bildirimler",
                subtitle = "Uyarıları aç/kapat",
                icon = Icons.Rounded.Notifications,
                trailingContent = {
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { isNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray.copy(alpha = 0.4f)
                        )
                    )
                }
            )

            // Hesap Bilgileri
            SettingsItemCard(
                title = "Hesap Bilgileri",
                subtitle = "Profil ve detaylar",
                icon = Icons.Rounded.AccountCircle,
                onClick = { /* Navigate to Account */ }
            )

            // Gizlilik
            SettingsItemCard(
                title = "Gizlilik",
                subtitle = "Veri koruma",
                icon = Icons.Rounded.VerifiedUser,
                onClick = { /* Navigate to Privacy */ }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 3. Çıkış Yap Butonu
        Button(
            onClick = { /* Logout Action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(2.dp, CircleShape, spotColor = DangerRed.copy(alpha = 0.2f)),
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceWhite,
                contentColor = DangerRed
            ),
            shape = CircleShape,
            border = BorderStroke(2.dp, DangerBorder)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Çıkış Yap",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

// --- Yardımcı Bileşen (Tekrar Kullanılabilir Kart) ---
@Composable
fun SettingsItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null, // Tıklama opsiyonel (Switch varsa tıklama olmayabilir)
    trailingContent: @Composable (() -> Unit)? = null // Sağ taraftaki içerik (Chevron veya Switch)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.LightGray.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol İkon (Yuvarlak Arkaplanlı)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Orta Metinler
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSub
                    )
                )
            }

            // Sağ Taraf (Opsiyonel İçerik veya Ok)
            if (trailingContent != null) {
                trailingContent()
            } else {
                // Varsayılan olarak sağ ok göster
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Go",
                    tint = Color.LightGray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsUiPreview() {
    SettingsUi()
}