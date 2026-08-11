package com.example.studysyncandroid.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object DeckList : Screen("deck_list")
    data object GenerateDeck : Screen("generate_deck")
    data object Review : Screen("review/{deckId}") {
        fun createRoute(deckId: String) = "review/$deckId"
    }
    data object Rooms : Screen("rooms")
    data object FolderDecks : Screen("folder_decks/{folderId}") {
        fun createRoute(folderId: String) = "folder_decks/$folderId"
    }
    data object Marketplace : Screen("marketplace")
    data object Analytics : Screen("analytics")
    data object RetentionCurve : Screen("retention_curve")
    data object LibraryStatus : Screen("library_status")
    data object UpcomingReviews : Screen("upcoming_reviews")
    data object Profile : Screen("profile")
}