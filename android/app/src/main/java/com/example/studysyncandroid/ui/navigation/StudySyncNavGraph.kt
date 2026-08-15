package com.example.studysyncandroid.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studysyncandroid.R
import com.example.studysyncandroid.ui.auth.LoginScreen
import com.example.studysyncandroid.ui.auth.SignupScreen
import com.example.studysyncandroid.ui.auth.EmailVerificationScreen
import com.example.studysyncandroid.ui.auth.ForgotPasswordScreen
import com.example.studysyncandroid.ui.auth.ResetPasswordScreen
import com.example.studysyncandroid.ui.decks.DeckListScreen
import com.example.studysyncandroid.ui.decks.FolderDecksScreen
import com.example.studysyncandroid.ui.decks.GenerateDeckScreen
import com.example.studysyncandroid.ui.review.ReviewScreen
import com.example.studysyncandroid.ui.analytics.AnalyticsScreen
import com.example.studysyncandroid.ui.analytics.AnalyticsDashboardScreen
import com.example.studysyncandroid.ui.analytics.LibraryStatusScreen
import com.example.studysyncandroid.ui.analytics.UpcomingReviewsScreen
import com.example.studysyncandroid.ui.rooms.RoomsScreen
import com.example.studysyncandroid.ui.marketplace.MarketplaceScreen
import com.example.studysyncandroid.ui.profile.ProfileScreen
import com.example.studysyncandroid.ui.components.BottomDock
import com.example.studysyncandroid.ui.decks.FolderViewModel

@Composable
fun StudySyncNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier,
    folderViewModel: FolderViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val topLevelRoutes = listOf(
        Screen.DeckList.route,
        Screen.Marketplace.route,
        Screen.Rooms.route,
        Screen.Analytics.route
    )

    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val deckListBgColor = colorResource(id = R.color.deck_list_bg)
    val bgColor = when {
        currentRoute == Screen.DeckList.route || 
        currentRoute?.startsWith("folder_decks") == true || 
        currentRoute == Screen.Rooms.route ||
        currentRoute == Screen.Marketplace.route ||
        currentRoute == Screen.GenerateDeck.route ||
        currentRoute == Screen.Analytics.route ||
        currentRoute == Screen.RetentionCurve.route ||
        currentRoute == Screen.LibraryStatus.route ||
        currentRoute == Screen.UpcomingReviews.route ||
        currentRoute == Screen.Login.route ||
        currentRoute == Screen.Signup.route ||
        currentRoute == Screen.ForgotPassword.route ||
        currentRoute == Screen.Profile.route ||
        currentRoute?.startsWith("verify_email") == true ||
        currentRoute?.startsWith("reset_password") == true -> deckListBgColor
        else -> MaterialTheme.colorScheme.background
    }

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                BottomDock(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.DeckList.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCreateDeckClick = {
                        navController.navigate(Screen.GenerateDeck.route)
                    },
                    onCreateFolderClick = {
                        showCreateFolderDialog = true
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.DeckList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupSuccess = { email -> 
                        navController.navigate(Screen.VerifyEmail.createRoute(email)) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.VerifyEmail.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email").orEmpty()
                EmailVerificationScreen(
                    email = email,
                    onVerificationSuccess = {
                        navController.navigate(Screen.DeckList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateToReset = { email ->
                        navController.navigate(Screen.ResetPassword.createRoute(email)) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email").orEmpty()
                ResetPasswordScreen(
                    email = email,
                    onResetSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DeckList.route) {
                DeckListScreen(
                    onDeckClick = { deckId -> navController.navigate(Screen.Review.createRoute(deckId)) },
                    onFolderClick = { folderId -> navController.navigate(Screen.FolderDecks.createRoute(folderId)) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(Screen.GenerateDeck.route) {
                GenerateDeckScreen(
                    onDeckGenerated = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Review.route,
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()
                ReviewScreen(deckId = deckId)
            }

            composable(Screen.Rooms.route) {
                RoomsScreen()
            }

            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsDashboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRetentionCurveClick = { navController.navigate(Screen.RetentionCurve.route) },
                    onLibraryStatusClick = { navController.navigate(Screen.LibraryStatus.route) },
                    onUpcomingReviewsClick = { navController.navigate(Screen.UpcomingReviews.route) }
                )
            }

            composable(Screen.RetentionCurve.route) {
                AnalyticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LibraryStatus.route) {
                LibraryStatusScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UpcomingReviews.route) {
                UpcomingReviewsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onNavigateToResetPassword = { email ->
                        navController.navigate(Screen.ResetPassword.createRoute(email))
                    }
                )
            }

            composable(
                route = Screen.FolderDecks.route,
                arguments = listOf(navArgument("folderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val folderId = backStackEntry.arguments?.getString("folderId").orEmpty()
                FolderDecksScreen(
                    folderId = folderId,
                    onBack = { navController.popBackStack() },
                    onDeckClick = { deckId -> navController.navigate(Screen.Review.createRoute(deckId)) }
                )
            }
        }

        if (showCreateFolderDialog) {
            var folderName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = { Text("Create Folder") },
                text = {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                folderViewModel.createFolder(folderName)
                                showCreateFolderDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}