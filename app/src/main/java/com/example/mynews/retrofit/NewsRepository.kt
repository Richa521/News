package com.example.mynews.retrofit

import com.example.mynews.Room.NewsDao
import com.example.mynews.models.NewsArticle

class NewsRepository(private val newsDao: NewsDao) {
    private val apiService: NewsApiService = RetrofitInstance.apiService

    suspend fun getTopHundredHeadlines(): List<NewsArticle> {
        val localArticles = newsDao.getAllArticles()

        return if (localArticles.size >= 100) {
            localArticles.take(100)
        }
        else {
            val remainingCount = 100 - localArticles.size
            val pageSize = minOf(remainingCount, 100)

            val countries = listOf("us", "in")
            val combinedArticles = mutableListOf<NewsArticle>()

            for (country in countries) {
                val response = apiService.getTopHundredHeadlines(country, "0e8b867815194c32b82d8c28063b5962", pageSize)
                val remoteArticles = response.articles
                combinedArticles.addAll(remoteArticles)

                newsDao.insertArticles(remoteArticles)

                if (combinedArticles.size >= 100) {
                    break
                }
            }

            combinedArticles.take(100)
        }
    }

    suspend fun getRandomHeadlines(countries: List<String>): List<NewsArticle> {
        val storedHeadlines = newsDao.getAllArticles()

        return if (storedHeadlines.isNotEmpty()) {
            val randomHeadlines = storedHeadlines.shuffled().take(5)
            randomHeadlines

        }
        else {
            val combinedArticles = mutableListOf<NewsArticle>()

            for (country in countries) {
                val response = apiService.getTopHundredHeadlines(country, "0e8b867815194c32b82d8c28063b5962", 5)
                val newHeadlines = response.articles
                newsDao.insertArticles(newHeadlines)

                combinedArticles.addAll(newHeadlines)
            }

            combinedArticles.take(5)
        }
    }



}
