package com.example.timelimit.core.data.repository

import com.example.timelimit.core.data.database.QuoteDao
import com.example.timelimit.core.data.database.QuoteEntity
import com.example.timelimit.core.data.remote.FocusQuoteApi
import com.example.timelimit.core.model.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusQuoteRepository @Inject constructor(
    private val api: FocusQuoteApi,
    private val quoteDao: QuoteDao
) {
    /**
     * Observes quotes from the local database. This is the single source of truth.
     */
    fun observeQuotes(): Flow<List<Quote>> = quoteDao.getQuotes().map { entities ->
        entities.map { it.toExternalModel() }
    }

    /**
     * Refreshes the local cache from the network.
     * Returns a Result indicating success or failure of the network call.
     */
    suspend fun refreshQuotes(): Result<Unit> {
        return try {
            val response = api.getQuotes()
            // Clear old cache and insert new quotes
            quoteDao.deleteAll()
            quoteDao.insertQuotes(response.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            // If network fails, we just return the failure; the UI continues to show cached data
            Result.failure(e)
        }
    }

    /**
     * Checks if the cache is stale (older than 1 hour).
     */
    suspend fun isCacheStale(): Boolean {
        val count = quoteDao.getCount()
        if (count == 0) return true
        
        // Simple staleness check: if the first quote is older than 1 hour
        // (This is a simplified approach)
        val latestQuote = quoteDao.getQuotes().firstOrNull()?.firstOrNull() ?: return true
        val oneHourAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        return latestQuote.cachedAt < oneHourAgo
    }
}

/**
 * Extension functions for mapping between local and external models.
 */
private fun QuoteEntity.toExternalModel() = Quote(
    text = text,
    author = author
)

private fun Quote.toEntity() = QuoteEntity(
    text = text,
    author = author
)
