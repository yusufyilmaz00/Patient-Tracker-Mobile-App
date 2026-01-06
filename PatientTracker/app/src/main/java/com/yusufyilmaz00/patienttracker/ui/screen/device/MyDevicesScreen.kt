package com.yusufyilmaz00.patienttracker.ui.screen.device

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufyilmaz00.patienttracker.MainViewModel

@Composable
fun MyDevicesScreen(
    mainViewModel: MainViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by mainViewModel.monitorUiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MyDevicesUi(
            state = uiState,
            onScanClick = {
                mainViewModel.startBleScan()
            },
            onDisconnectClick = {
                mainViewModel.disconnectBle()
            },
            onBackClick = onNavigateBack,
            onDeviceClick = { device ->
                mainViewModel.connectToDevice(device)
            }
        )
    }
}