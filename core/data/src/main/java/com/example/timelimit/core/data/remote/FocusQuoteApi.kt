package com.example.timelimit.core.data.remote

import com.example.timelimit.core.model.Quote
import retrofit2.http.GET

interface FocusQuoteApi {
    @GET("quotes")
    suspend fun getQuotes(): List<Quote>
}
