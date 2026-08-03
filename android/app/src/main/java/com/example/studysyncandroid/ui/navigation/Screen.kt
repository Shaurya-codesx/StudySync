package com.example.studysyncandroid.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object DeckList : Screen("deck_list")
    data object Review : Screen("review/{deckId}") {
        fun createRoute(deckId: String) = "review/$deckId"
    }
    data object Rooms : Screen("rooms")
}