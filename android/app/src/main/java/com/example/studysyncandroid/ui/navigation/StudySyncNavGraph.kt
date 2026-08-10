package com.example.studysyncandroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studysyncandroid.ui.auth.LoginScreen
import com.example.studysyncandroid.ui.auth.SignupScreen
import com.example.studysyncandroid.ui.decks.DeckListScreen
import com.example.studysyncandroid.ui.decks.FolderDecksScreen
import com.example.studysyncandroid.ui.decks.GenerateDeckScreen
import com.example.studysyncandroid.ui.review.ReviewScreen
import com.example.studysyncandroid.ui.rooms.RoomsScreen
import com.example.studysyncandroid.ui.marketplace.MarketplaceScreen

@Composable
fun StudySyncNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DeckList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.DeckList.route) {
            DeckListScreen(
                onDeckClick = { deckId -> navController.navigate(Screen.Review.createRoute(deckId)) },
                onFolderClick = { folderId -> navController.navigate(Screen.FolderDecks.createRoute(folderId)) },
                onGenerateDeckClick = { navController.navigate(Screen.GenerateDeck.route) },
                onRoomsClick = { navController.navigate(Screen.Rooms.route) },
                onMarketplaceClick = { navController.navigate(Screen.Marketplace.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.GenerateDeck.route) {
            GenerateDeckScreen(
                onDeckGenerated = { navController.popBackStack() }
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
            MarketplaceScreen()
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
}