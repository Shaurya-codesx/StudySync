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
import com.example.studysyncandroid.ui.decks.DeckListScreen
import com.example.studysyncandroid.ui.review.ReviewScreen
import com.example.studysyncandroid.ui.rooms.RoomsScreen

@Composable
fun StudySyncNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DeckList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DeckList.route) {
            DeckListScreen(
                onDeckClick = { deckId -> navController.navigate(Screen.Review.createRoute(deckId)) },
                onRoomsClick = { navController.navigate(Screen.Rooms.route) }
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
    }
}