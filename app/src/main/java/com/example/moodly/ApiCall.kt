package com.example.moodly

import com.example.moodly.ApiService
import com.example.moodly.QuoteModel
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCall {
    // fetch the Quotes from the API
    fun getRandomQuotes(callback: (List<QuoteModel>?) -> Unit) {

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("https://type.fit/api/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create ApiService interface
        val api = retrofit.create(ApiService::class.java)

        // Make an API call to get random quotes
        api.getRandomQuotes().enqueue(object : Callback<List<QuoteModel>> {

            // Callback method on successful API response
            override fun onResponse(
                call: Call<List<QuoteModel>>,
                response: Response<List<QuoteModel>>
            ) {
                if (response.isSuccessful) {
                    val quoteslist: List<QuoteModel>? = response.body()
                    callback(quoteslist)
                } else {
                    callback(null)
                }
            }

            // Callback method on API call failure
            override fun onFailure(call: Call<List<QuoteModel>>, t: Throwable) {
                callback(null)
            }
        })
    }
}