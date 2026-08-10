package com.example.studysyncandroid.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.studysyncandroid.data.remote.MarketplaceApi
import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse

class MarketplacePagingSource(
    private val marketplaceApi: MarketplaceApi
) : PagingSource<Int, DeckSummaryResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DeckSummaryResponse> {
        return try {
            val page = params.key ?: 1
            val limit = params.loadSize

            val response = marketplaceApi.getPublicDecks(page, limit)

            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= response.totalPages) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DeckSummaryResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
