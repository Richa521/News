package com.example.mynews.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import okhttp3.OkHttpClient


object RetrofitInstance {
    private const val BASE_URL = "https://newsapi.org"

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: NewsApiService = retrofit.create(NewsApiService::class.java)
}