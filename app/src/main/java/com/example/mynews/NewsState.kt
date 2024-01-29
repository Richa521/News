package com.example.mynews

import com.example.mynews.models.NewsArticle


sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<NewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}

