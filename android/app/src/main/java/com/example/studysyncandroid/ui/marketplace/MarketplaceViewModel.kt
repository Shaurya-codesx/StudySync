package com.example.studysyncandroid.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse
import com.example.studysyncandroid.data.repository.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.studysyncandroid.util.toUserFriendlyMessage

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    val publicDecks: Flow<PagingData<DeckSummaryResponse>> =
        marketplaceRepository.getPublicDecks().cachedIn(viewModelScope)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun cloneDeck(deckId: String) {
        viewModelScope.launch {
            marketplaceRepository.cloneDeck(deckId)
                .onSuccess { _message.value = "Deck successfully cloned to your library!" }
                .onFailure { _message.value = it.toUserFriendlyMessage() }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
