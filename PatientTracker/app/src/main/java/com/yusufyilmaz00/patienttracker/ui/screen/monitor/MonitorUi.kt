package com.yusufyilmaz00.patienttracker.ui.screen.monitor

import android.R.attr.data
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yusufyilmaz00.patienttracker.ui.theme.BgLight
import com.yusufyilmaz00.patienttracker.ui.theme.DangerBg
import com.yusufyilmaz00.patienttracker.ui.theme.DangerRed
import com.yusufyilmaz00.patienttracker.ui.theme.DangerRing
import com.yusufyilmaz00.patienttracker.ui.theme.PrimaryBlue
import com.yusufyilmaz00.patienttracker.ui.theme.SuccessGreen
import com.yusufyilmaz00.patienttracker.ui.theme.TextDark
import com.yusufyilmaz00.patienttracker.ui.theme.TextGray
import com.yusufyilmaz00.patienttracker.ui.theme.WarningOrange


@Composable
fun MonitorUi(
    state: MonitorUiState,           // UI artık sadece bu State'e bakar
    onDismissAlert: () -> Unit       // Pop-up kapatılınca ne olacağını üst katmana bildirir
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // --- Ana İçerik ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgLight)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 80.dp)
        ) {
            // 1. Üst Bağlantı Durumu
            StatusHeaderSection(isConnected = state.isDeviceConnected)

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Nabız Kartı (State'den gelen nabız değeri)
            HeartRateMainCard(bpm = state.heartRate)

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Grafik Alanı
            ChartSection()

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Bildirimler Başlığı
            Text(
                text = "Recent Notifications",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 5. Bildirim Listesi (State'den gelen liste)
            state.notifications.forEach { notification ->
                NotificationItem(notification)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // --- Pop-up Kontrolü ---
        // State içindeki değişken true ise pop-up açılır
        if (state.showEmergencyPopup) {
            AlertPopup(
                onDismiss = onDismissAlert
            )
        }
    }
}

// --- Alt Bileşenler (Composables) ---

@Composable
fun StatusHeaderSection(isConnected: Boolean) {
    val statusText = if (isConnected) "Device Connected" else "Device Disconnected"
    val statusColor = if (isConnected) SuccessGreen else DangerRed

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            )
        }
        TextButton(onClick = { /* Cihaz değiştir */ }) {
            Text("Change Device", color = PrimaryBlue)
        }
    }
}

@Composable
fun HeartRateMainCard(bpm: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.LightGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFEBF5FF), Color.White),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            ))

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Heart",
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HEART RATE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = bpm.toString(), // State'den gelen veri
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 80.sp
                        ),
                        lineHeight = 80.sp
                    )
                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextGray,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChartSection() {
    // Listeye 40 ve 20'yi ekledik
    val yAxisLabels = listOf("120", "100", "80", "60", "40", "20")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last 60 Seconds",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            )
            Surface(
                color = BgLight,
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = "Live",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grafik Kartı
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Kart boyunu biraz daha artırdım (180 -> 200) ki sayılar rahat sığsın
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. SOL TARAFTAKİ SAYILAR (Y-AXIS)
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yAxisLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextGray,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. GRAFİK ALANI
                // Grid çizgi sayısı artık 6 olacak ve çizgiler otomatik sıklaşacak
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    MockWaveChart(gridLineCount = yAxisLabels.size)
                }
            }
        }
    }
}

@Composable
fun MockWaveChart(gridLineCount: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. IZGARA ÇİZGİLERİNİ ÇİZ (Grid Lines)
        // Sayılar SpaceBetween ile yerleştiği için çizgileri de ona göre hesaplıyoruz.
        // İlk çizgi en üstte (0), son çizgi en altta (height) olacak şekilde.
        if (gridLineCount > 1) {
            val stepHeight = height / (gridLineCount - 1)
            for (i in 0 until gridLineCount) {
                val yPos = stepHeight * i
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, yPos),
                    end = Offset(width, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // 2. DALGA GRAFİĞİ (WAVE)
        // Not: Grafiğin çizgilerin dışına taşmaması için biraz padding payı bırakılabilir,
        // ama görsel şıklık için full width kullanıyoruz.
        val path = Path().apply {
            // Başlangıç noktası (Soldan, ortanın biraz altından başla)
            moveTo(0f, height * 0.75f)

            // Bezier eğrileriyle dalga oluşturma
            cubicTo(width * 0.1f, height * 0.75f, width * 0.15f, height * 0.45f, width * 0.25f, height * 0.50f)
            cubicTo(width * 0.35f, height * 0.55f, width * 0.4f, height * 0.35f, width * 0.5f, height * 0.40f)
            cubicTo(width * 0.6f, height * 0.45f, width * 0.65f, height * 0.70f, width * 0.75f, height * 0.65f)
            cubicTo(width * 0.85f, height * 0.60f, width * 0.9f, height * 0.30f, width, height * 0.35f)
        }

        // Çizgi Altı Gradient (Dolgu)
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height) // Sağ alt köşe
            lineTo(0f, height)    // Sol alt köşe
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(PrimaryBlue.copy(alpha = 0.2f), PrimaryBlue.copy(alpha = 0f)),
                startY = 0f,
                endY = height
            )
        )

        // Ana Mavi Çizgi
        drawPath(
            path = path,
            color = PrimaryBlue,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Sondaki canlı nokta (Pulse)
        drawCircle(
            color = PrimaryBlue,
            radius = 4.dp.toPx(),
            center = Offset(width, height * 0.35f)
        )

        // Noktanın etrafındaki hafif hare (Ping efekti statik hali)
        drawCircle(
            color = PrimaryBlue.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = Offset(width, height * 0.35f)
        )
    }
}

@Composable
fun NotificationItem(notification: MonitorNotification) {
    // Enum türüne göre stil belirleme
    val (bgColor, iconColor, icon) = when (notification.type) {
        NotificationType.EMERGENCY -> Triple(Color(0xFFFEF2F2), DangerRed, Icons.Rounded.Warning) // Kırmızı ton
        NotificationType.FALL_DETECTED -> Triple(Color(0xFFFFF7ED), WarningOrange, Icons.Rounded.Person) // Turuncu ton
        NotificationType.WARNING -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Icons.Rounded.Info) // Sarı ton
        NotificationType.INFO -> Triple(BgLight, TextGray, Icons.Rounded.BatteryFull) // Gri/Mavi ton
        NotificationType.SUCCESS -> Triple(Color(0xFFF0FDF4), SuccessGreen, Icons.Rounded.CheckCircle) // Yeşil ton
    }

    // INFO tipinde heart icon özel durumu
    val finalIcon = if (notification.title.contains("Heart")) Icons.Rounded.Favorite else icon
    val finalIconColor = if (notification.title.contains("Heart")) PrimaryBlue else iconColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = finalIcon,
                contentDescription = null,
                tint = finalIconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.time,
                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
            )
        }
    }
}



@Composable
fun AlertPopup(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Dialog bileşeni, arkadaki ekranın üzerine modal olarak açılır.
    // usePlatformDefaultWidth = false yaparak tam ekran kontrolü sağlıyoruz (blur efekti için).
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // 1. Arka Plan (Blur ve Karartma Efekti)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // Karartma
                // Not: Modifier.blur(10.dp) Android 12+ gerektirir.
                // Eski sürümler için sadece karartma yeterlidir.
                .padding(24.dp), // Kartın kenarlardan boşluğu
            contentAlignment = Alignment.Center
        ) {

            // 2. Pop-up Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // İkon Alanı (Halkalı ve Kırmızı)
                    Box(
                        modifier = Modifier
                            .size(80.dp) // Dış halka boyutu
                            .background(DangerBg, CircleShape)
                            .border(6.dp, DangerRing, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning, // emergency_home yerine standart warning
                            contentDescription = "Emergency",
                            tint = DangerRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Başlık ve Açıklama
                    Text(
                        text = "Emergency Alert",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The emergency button on the device was pressed just now.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextGray,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Butonlar

                    // Kırmızı Ar (112) Butonu
                    Button(
                        onClick = {
                            // 112 Arama İntenti
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:112")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(8.dp, CircleShape, spotColor = DangerRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manage Emergency",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dismiss (Kapat) Butonu
                    OutlinedButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                    ) {
                        Text(
                            text = "OK, Dismiss",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

