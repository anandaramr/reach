package com.project.reach.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.project.reach.ui.components.BottomBar
import com.project.reach.ui.components.BottomNavBarItem
import com.project.reach.ui.screens.chat.ChatScreen
import com.project.reach.ui.screens.chat.ChatScreenDestination
import com.project.reach.ui.screens.contacts.ContactScreen
import com.project.reach.ui.screens.contacts.ContactScreenDestination
import com.project.reach.ui.screens.discover.DiscoverScreenDestination
import com.project.reach.ui.screens.discover.DiscoveryScreen
import com.project.reach.ui.screens.home.HomeScreen
import com.project.reach.ui.screens.home.HomeScreenDestination
import com.project.reach.ui.screens.newContact.NewContactScreen
import com.project.reach.ui.screens.newContact.NewContactScreenDestination
import com.project.reach.ui.screens.onboarding.OnboardingScreen
import com.project.reach.ui.screens.qrcode.QRCodeScreen
import com.project.reach.ui.screens.qrcode.QRCodeScreenDestination
import com.project.reach.ui.screens.settings.SettingsScreen
import com.project.reach.ui.screens.settings.SettingsScreenDestination
import com.project.reach.ui.screens.viewContact.ViewContactScreen
import com.project.reach.ui.screens.viewContact.ViewContactScreenDestination

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigationHost(
    navController: NavHostController = rememberNavController(),
    isOnboardingRequired: Boolean,
    startService: () -> Unit
) {
    val items = listOf(
        BottomNavBarItem("Home", Icons.Default.Home, "home"),
        BottomNavBarItem("Discover", Icons.Default.Search, "discover"),
        BottomNavBarItem("Settings", Icons.Default.Settings, "settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.destination?.route
    val displayBottomBar = currentScreen in items.map { it.route }

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            BottomBar(navController, items, isVisible = displayBottomBar)
        }

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingRequired) OnboardingScreen.route else HomeScreenDestination.route,
        ) {
            composable(route = DiscoverScreenDestination.route) {
                DiscoveryScreen(
                    navigateToChat = { peerId -> navController.navigate(ChatScreenDestination.createRoute(peerId)){
                        popUpTo(DiscoverScreenDestination.route){
                            inclusive = true
                        }
                    } },
                )
            }
            composable(route = ContactScreenDestination.route) {
                ContactScreen (
                    navigateToChat = { peerId -> navController.navigate(ChatScreenDestination.createRoute(peerId)); },
                    navigateBack = { navController.popBackStack(route = HomeScreenDestination.route, inclusive = false) },
                    navigateToNewContact = { userId,username ->  navController.navigate(NewContactScreenDestination.createRoute(userId, username))}
                )
            }

            composable(route = OnboardingScreen.route, exitTransition = { ExitTransition.None }) {
                OnboardingScreen(
                    onOnboardingComplete = { navController.navigate(route = HomeScreenDestination.route) {
                        popUpTo(OnboardingScreen.route) {
                            inclusive = true
                        }
                    } }
                )
            }
            composable(route = SettingsScreenDestination.route) {
                SettingsScreen(
                    navigateToQRCode = { userId,username ->  navController.navigate(QRCodeScreenDestination.createRoute(userId, username))},
                )
            }
            composable(route = HomeScreenDestination.route) {
                HomeScreen(
                    navigateToChat = { peerId -> navController.navigate(ChatScreenDestination.createRoute(peerId)) },
                    navigateToDiscover = { navController.navigate(DiscoverScreenDestination.route) },
                    navigateToContact = { navController.navigate(ContactScreenDestination.route) },
                    startService = startService
                )
            }
            composable(
                route = ChatScreenDestination.route,
                arguments = listOf(navArgument("peerId") { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "reach://${ChatScreenDestination.deepLinkPattern}" }
                )
            ) {
                ChatScreen(
                    navigateBack = { navController.popBackStack(route = HomeScreenDestination.route, inclusive = false) },
                    navigateToViewContact = { userId,username,nickname ->  navController.navigate(
                        ViewContactScreenDestination.createRoute(userId, username, nickname))}
                )
             }
            composable(
                route = QRCodeScreenDestination.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("username") { type = NavType.StringType }
                )
            ){ backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?:""
                val username = backStackEntry.arguments?.getString("username")?:""
                QRCodeScreen(
                    modifier = Modifier.padding(innerPadding),
                    navigateBack = { navController.navigate(SettingsScreenDestination.route) },
                    userId = userId,
                    username = username,
                )
            }
            composable(
                route = NewContactScreenDestination.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("username") { type = NavType.StringType }
                )
            ) {
                backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?:""
                val username = backStackEntry.arguments?.getString("username")?:""

                NewContactScreen(
                    navigateToChat = { peerId -> navController.navigate(ChatScreenDestination.createRoute(peerId)){
                        popUpTo(NewContactScreenDestination.route){
                            inclusive = true
                        }
                    } },
                    navigateBack = { navController.popBackStack() },
                    userId = userId,
                    username = username,
                )
            }
            composable(
                route = ViewContactScreenDestination.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("username") { type = NavType.StringType },
                    navArgument("nickname") { type = NavType.StringType }
                )
            ) {
                    backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?:""
                val username = backStackEntry.arguments?.getString("username")?:""
                val nickname = backStackEntry.arguments?.getString("nickname")?:""

                ViewContactScreen(
                    navigateToChat = { peerId -> navController.navigate(ChatScreenDestination.createRoute(peerId)); },
                    navigateBack = { navController.popBackStack() },
                    userId = userId,
                    username = username,
                    nickname =  nickname
                )
            }
        }
    }
}

