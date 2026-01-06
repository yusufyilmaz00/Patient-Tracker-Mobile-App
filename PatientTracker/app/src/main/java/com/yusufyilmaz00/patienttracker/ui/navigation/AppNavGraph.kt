package com.yusufyilmaz00.patienttracker.ui.navigation

import com.yusufyilmaz00.patienttracker.ui.screen.device.MyDevicesScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yusufyilmaz00.patienttracker.MainViewModel
import com.yusufyilmaz00.patienttracker.ui.screen.monitor.MonitorScreen
import com.yusufyilmaz00.patienttracker.ui.screen.setting.SettingsScreen

// Sealed class'ı artık gezinme grafiğiyle ilgili olduğu için bu dosyaya taşıyabiliriz.
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Monitor : Screen(Route.MONITOR, "Monitor", Icons.Default.Home)
    data object MyDevices : Screen(Route.MY_DEVICES, "My Devices", Icons.Default.Smartphone)
    data object Settings : Screen(Route.SETTINGS, "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavGraph(
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()

    val monitorState by mainViewModel.monitorUiState.collectAsState()
    val showEmergency = monitorState.showEmergencyPopup

    LaunchedEffect(showEmergency) {
        if (showEmergency) {
            // Eğer mevcut ekran "Monitor" değilse, oraya git.
            if (navController.currentDestination?.route != Route.MONITOR) {
                navController.navigate(Route.MONITOR) {
                    // Geri yığınını temizleyerek kullanıcının geri tuşuyla
                    // önceki ekrana dönmesini engelle.
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Navigasyon çubuğu için sayfa bilgilerini tutan liste
    val items = listOf(
        Screen.Monitor,
        Screen.MyDevices,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Geri tuşuna basıldığında yığının en başına dönmeyi sağlar
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Aynı sekmeye tekrar tıklamayı engeller
                                launchSingleTop = true
                                // Sekmeler arasında geçiş yaparken durumu korur
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Sayfaların gösterileceği alan
        NavHost(
            navController = navController,
            startDestination = Route.MONITOR, // Başlangıç ekranı
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.MONITOR) {
                MonitorScreen(
                state = monitorState,
                onDismissAlert = { mainViewModel.dismissEmergencyAlert() }
                )
            }
            composable(Route.MY_DEVICES) {
                MyDevicesScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Route.SETTINGS) { SettingsScreen() }
        }
    }
}
