package com.example.studysyncandroid.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.repository.CardRepository
import com.example.studysyncandroid.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.studysyncandroid.util.toUserFriendlyMessage

data class ReviewUiState(
    val isLoading: Boolean = true,
    val deckTitle: String? = null,
    val dueCards: List<CardEntity> = emptyList(),
    val currentCardIndex: Int = 0,
    val isAnswerRevealed: Boolean = false,
    val error: String? = null
) {
    val isReviewFinished: Boolean
        get() = !isLoading && dueCards.isNotEmpty() && currentCardIndex >= dueCards.size

    val currentCard: CardEntity?
        get() = if (currentCardIndex < dueCards.size) dueCards[currentCardIndex] else null

    val hasNoDueCards: Boolean
        get() = !isLoading && dueCards.isEmpty()
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun loadDueCards(deckId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Fetch the deck to get its title
                val deckTitle = deckRepository.getDecksStream().firstOrNull()?.find { it.id == deckId }?.title
                    ?: "Review"
                val cards = cardRepository.getDueCards(deckId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        deckTitle = deckTitle,
                        dueCards = cards,
                        currentCardIndex = 0,
                        isAnswerRevealed = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.toUserFriendlyMessage())
                }
            }
        }
    }

    fun revealAnswer() {
        _uiState.update { it.copy(isAnswerRevealed = true) }
    }

    fun submitRating(quality: Int) {
        val currentCard = _uiState.value.currentCard ?: return

        viewModelScope.launch {
            // Send to server and update local Room DB cache
            val result = cardRepository.reviewCard(currentCard.id, quality)

            if (result.isSuccess) {
                // Advance to the next card and hide the answer for the new card
                _uiState.update { state ->
                    state.copy(
                        currentCardIndex = state.currentCardIndex + 1,
                        isAnswerRevealed = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(error = "Failed to submit review. Check connection.")
                }
            }
        }
    }
}