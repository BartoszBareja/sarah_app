package com.example.sarah_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sarah_app.data.repository.ServerRepository
import com.example.sarah_app.ui.about.AboutSarahScreen
import com.example.sarah_app.ui.breathing.BreathingScreen
import com.example.sarah_app.ui.chat.ChatHistoryScreen
import com.example.sarah_app.ui.chat.ChatScreen
import com.example.sarah_app.ui.components.SharedTopBar
import com.example.sarah_app.ui.home.HomeScreen
import com.example.sarah_app.ui.gallery.GalleryScreen
import com.example.sarah_app.ui.pairing.PairingScreen
import com.example.sarah_app.ui.profile.ProfileScreen
import com.example.sarah_app.ui.rateus.RateUsScreen
import com.example.sarah_app.ui.settings.SettingsScreen
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.PrimaryOrange
import com.example.sarah_app.ui.theme.Sarah_appTheme
import com.example.sarah_app.ui.theme.SurfaceLight
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class NavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    NavItem("home", "Home", Icons.Default.Home),
    NavItem("chat", "Chat", Icons.AutoMirrored.Filled.Chat),
    NavItem("gallery", "Gallery", Icons.Default.PhotoLibrary),
    NavItem("settings", "Settings", Icons.Default.Settings),
)

val screenTitles = mapOf(
    "home" to "Home",
    "chat" to "Talk to Sarah",
    "chat_history" to "Chat History",
    "breathing" to "Breathing Exercises",
    "profile" to "Profile",
    "settings" to "Settings",
    "rate_us" to "Rate Us",
    "about_sarah" to "About Sarah",
    "gallery" to "Gallery",
    "pairing" to "Server Connection",
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var serverRepository: ServerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sarah_appTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val url = serverRepository.serverUrl.first()
                    startDestination = if (url == null) "pairing" else "home"
                }

                if (startDestination == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundCream),
                    )
                    return@Sarah_appTheme
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: startDestination!!
                val currentTitle = screenTitles[currentRoute] ?: ""

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        SharedTopBar(
                            title = currentTitle,
                            showHamburger = currentRoute == "chat" || currentRoute == "chat_history",
                            onHamburgerClick = {
                                if (currentRoute == "chat") {
                                    navController.navigate("chat_history")
                                } else {
                                    navController.navigateUp()
                                }
                            },
                            onAvatarClick = {
                                if (currentRoute != "profile") {
                                    navController.navigate("profile")
                                }
                            },
                        )
                    },
                    bottomBar = {
                        NavigationBar(containerColor = SurfaceLight) {
                            val currentDestination = navBackStackEntry?.destination
                            bottomNavItems.forEach { item ->
                                val selected =
                                    currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo("home") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryOrange,
                                        selectedTextColor = PrimaryOrange,
                                        unselectedIconColor = BrownText,
                                        unselectedTextColor = BrownText,
                                        indicatorColor = BackgroundCream,
                                    ),
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToBreathing = { navController.navigate("breathing") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToChat = {
                                    navController.navigate("chat") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToRateUs = { navController.navigate("rate_us") },
                                onNavigateToAbout = { navController.navigate("about_sarah") },
                            )
                        }
                        composable("chat") { ChatScreen() }
                        composable("chat_history") { ChatHistoryScreen() }
                        composable("breathing") { BreathingScreen() }
                        composable("profile") { ProfileScreen() }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateToPairing = { navController.navigate("pairing") },
                            )
                        }
                        composable("rate_us") { RateUsScreen() }
                        composable("about_sarah") { AboutSarahScreen() }
                        composable("gallery") { GalleryScreen() }
                        composable("pairing") { PairingScreen() }
                    }
                }
            }
        }
    }
}
