package com.example.studysyncandroid.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.repository.DeckRepository
import com.example.studysyncandroid.util.toUserFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ManageDecksUiState {
    data object Loading : ManageDecksUiState
    data class Success(val decks: List<DeckEntity>) : ManageDecksUiState
    data class Error(val message: String) : ManageDecksUiState
}

@HiltViewModel
class ManagePublicDecksViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {

    val uiState: StateFlow<ManageDecksUiState> = deckRepository.getDecksStream()
        .map { decks ->
            ManageDecksUiState.Success(decks.filter { it.isPublic }) as ManageDecksUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ManageDecksUiState.Loading
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            deckRepository.refreshDecks()
                .onFailure { error ->
                    _errorMessage.value = error.toUserFriendlyMessage()
                }
        }
    }

    fun unpublishDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.unpublishDeck(deckId)
                .onSuccess {
                    deckRepository.refreshDecks() // Refresh to update local DB
                }
                .onFailure { error ->
                    _errorMessage.value = error.toUserFriendlyMessage()
                }
        }
    }
}
