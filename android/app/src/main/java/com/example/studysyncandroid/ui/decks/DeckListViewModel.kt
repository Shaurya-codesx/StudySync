package com.example.studysyncandroid.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {

    val decks: StateFlow<List<DeckEntity>> = deckRepository.getDecksStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            deckRepository.refreshDecks()
                .onFailure { _errorMessage.value = it.message ?: "Failed to refresh decks" }
            _isRefreshing.value = false
        }
    }

    fun moveDeckToFolder(deckId: String, folderId: String?) {
        viewModelScope.launch {
            deckRepository.moveDeckToFolder(deckId, folderId)
                .onFailure { _errorMessage.value = it.message ?: "Failed to move deck" }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
                .onFailure { _errorMessage.value = it.message ?: "Failed to delete deck" }
        }
    }

    fun publishDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.publishDeck(deckId)
                .onSuccess { 
                    _errorMessage.value = "Deck published to Marketplace!" 
                    refresh()
                }
                .onFailure { _errorMessage.value = it.message ?: "Failed to publish deck" }
        }
    }

    fun unpublishDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.unpublishDeck(deckId)
                .onSuccess { 
                    _errorMessage.value = "Deck removed from Marketplace!" 
                    refresh()
                }
                .onFailure { _errorMessage.value = it.message ?: "Failed to unpublish deck" }
        }
    }
}