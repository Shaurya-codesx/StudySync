package com.example.studysyncandroid.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.studysyncandroid.data.local.dao.DeckDao
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.remote.MarketplaceApi
import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MarketplaceRepository @Inject constructor(
    private val marketplaceApi: MarketplaceApi,
    private val deckDao: DeckDao
) {
    fun getPublicDecks(): Flow<PagingData<DeckSummaryResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MarketplacePagingSource(marketplaceApi) }
        ).flow
    }

    suspend fun cloneDeck(deckId: String): Result<String> = runCatching {
        val response = marketplaceApi.cloneDeck(deckId)
        
        // Cache the newly cloned deck locally
        deckDao.insertDeck(
            DeckEntity(
                id = response.id,
                title = response.title,
                cardCount = response.cardCount,
                createdAt = response.createdAt,
                folderId = null
            )
        )
        
        response.id
    }
}
