package com.yusufyilmaz00.patienttracker.ui.screen.monitor

import androidx.compose.runtime.Composable

@Composable
fun MonitorScreen(
    state: MonitorUiState,
    onDismissAlert: () -> Unit
) {
    // 3. UI Bileşenini Çağır ve State'i Ver
    MonitorUi(
        state = state,
        onDismissAlert = onDismissAlert
    )
}