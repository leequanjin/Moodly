package com.example.moodly

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // Get request
    @GET("quotes")
    // function that return a List of QuotesModel Object
    fun getRandomQuotes(): Call<List<QuoteModel>>
}